package ua.kpi.ipsa.route

import org.slf4j.LoggerFactory
import sttp.monad.MonadError
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.interceptor.{EndpointInterceptor, RequestHandler, RequestInterceptor, Responder}
import sttp.tapir.server.ziohttp.{ZioHttpServerOptions, ZioHttpServerRequest}
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{Header, Request}
import zio.{RIO, UIO}

import java.util.UUID

object syntax {
  val X_REQUEST_ID = "x-request-id"

  implicit final class ZioHttpServerOptionsSyntax[R](private val opts: ZioHttpServerOptions[R]) extends AnyVal {
    def prependXRequestIdInterceptor(nextRequestId: () => UIO[String]): ZioHttpServerOptions[R] = {
      opts.prependInterceptor(new RequestInterceptor[RIO[R, *]] {
        override def apply[B](
            responder: Responder[RIO[R, *], B],
            requestHandler: EndpointInterceptor[RIO[R, *]] => RequestHandler[RIO[R, *], B]
        ): RequestHandler[RIO[R, *], B] = {
          RequestHandler.from[RIO[R, *], B] { (request, monad) =>
            implicit val m: MonadError[RIO[R, *]] = monad
            for {
              underlying <- UIO(request.underlying.asInstanceOf[Request])
              nextRequest <- underlying.getHeaderValue(X_REQUEST_ID) match {
                               case None =>
                                 nextRequestId().map(xId => {
                                   val withXId = underlying.copy(headers = underlying.headers :+ Header.custom(X_REQUEST_ID, xId))
                                   new ZioHttpServerRequest(withXId): ServerRequest
                                 })
                               case Some(_) =>
                                 UIO(request)
                             }
              res <- requestHandler(EndpointInterceptor.noop[RIO[R, *]]).apply(nextRequest)
            } yield res
          }
        }
      })
    }
  }

  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)
  def ctx(request: ServerRequest): Ctx = {
    request.underlying.asInstanceOf[Request].getHeaderValue(X_REQUEST_ID) match {
      case Some(xId) =>
        Ctx(xId)
      case None =>
        zio.Runtime.default.unsafeRun(for {
          ctx <- UIO(Ctx(UUID.randomUUID().toString))
          _   <- ua.kpi.ipsa.trace.log.warn("no request id in request header, ensure setup x-request-id middleware, default uuid generation used")(ctx, logger)
        } yield ctx)
    }
  }
}
