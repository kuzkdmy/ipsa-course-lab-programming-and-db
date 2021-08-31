package ua.kpi.ipsa.route

import org.slf4j.LoggerFactory
import sttp.model.StatusCode
import sttp.tapir.server.interceptor.ValuedEndpointOutput
import sttp.tapir.server.interceptor.exception.{ExceptionContext, ExceptionHandler}
import sttp.tapir.{statusCode, stringBody}
import ua.kpi.ipsa.route.syntax.{ctx, X_REQUEST_ID}
import ua.kpi.ipsa.trace.{log, Ctx}

case class AppExceptionHandler(response: (StatusCode, String) => ValuedEndpointOutput[_]) extends ExceptionHandler {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)
  override def apply(exCtx: ExceptionContext): Option[ValuedEndpointOutput[_]] = {
    implicit val c: Ctx = ctx(exCtx.request)
    zio.Runtime.default.unsafeRun(log.error("Server Error", exCtx.e))
    Some(response(StatusCode.InternalServerError, s"""{"error":"Internal server error","$X_REQUEST_ID":"${c.requestId}"}"""))
  }
}
object AppExceptionHandler {
  val handler: AppExceptionHandler = AppExceptionHandler((sc, m) => ValuedEndpointOutput(statusCode.and(stringBody), (sc, m)))
}
