package ua.kpi.ipsa.route.middleware

import sttp.tapir.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.interceptor.CustomInterceptors
import sttp.tapir.server.interceptor.log.{DefaultServerLog, ServerLogInterceptor}
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import sttp.tapir.server.ziohttp.ZioHttpServerOptions.{defaultCreateFile, defaultDeleteFile}
import ua.kpi.ipsa.MainApp.logger
import ua.kpi.ipsa.route.middleware.syntax.ZioHttpServerOptionsSyntax
import zio.metrics.prometheus.Registry
import zio.{RIO, UIO, URIO, ZLayer}

import java.util.UUID

trait ServerOptionsService {
  def serverOptions[R]: UIO[ZioHttpServerOptions[R]]
}

class ServerOptionsServiceLive(registry: Registry.Service) extends ServerOptionsService {
  def serverOptions[R]: UIO[ZioHttpServerOptions[R]] = {
    registry
      .getCurrent()
      .map(collectorRegistry => {
        CustomInterceptors[RIO[R, *], UIO[Unit], ZioHttpServerOptions[R]](
          createLogInterceptor = sl => new ServerLogInterceptor[UIO[Unit], RIO[R, *]](sl, (t, _) => t),
          createOptions        = ci => ZioHttpServerOptions[R](defaultCreateFile, defaultDeleteFile, ci.interceptors),
          metricsInterceptor   = Some(PrometheusMetrics[RIO[R, *]]("tapir", collectorRegistry).metricsInterceptor()),
          exceptionHandler     = Some(AppExceptionHandler.handler),
          serverLog = Some(
            DefaultServerLog(
              doLogWhenHandled       = (m, errOpt) => UIO(logger.info(s"request handled $m $errOpt")),
              doLogAllDecodeFailures = (m, errOpt) => UIO(logger.info(s"decode failure $m $errOpt")),
              doLogExceptions        = (m, err) => UIO(logger.error(m, err)),
              noLog                  = UIO.unit,
              logWhenHandled         = true,
              logAllDecodeFailures   = false,
              logLogicExceptions     = true
            )
          )
        ).options.prependXRequestIdInterceptor(() => URIO(UUID.randomUUID().toString))
      })
  }
}

object ServerOptionsServiceLive {
  val layer = ZLayer.fromService[
    Registry.Service,
    ServerOptionsService
  ](new ServerOptionsServiceLive(_))
}
