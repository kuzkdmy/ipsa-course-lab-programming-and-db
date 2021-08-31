package ua.kpi.ipsa

import io.prometheus.client.CollectorRegistry
import org.slf4j.LoggerFactory
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.metrics.prometheus.PrometheusMetrics
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.swagger.ziohttp.SwaggerZioHttp
import ua.kpi.ipsa.config.AppConfig
import ua.kpi.ipsa.repository.{DBLayer, HotelStarsRepositoryLive}
import ua.kpi.ipsa.route.HotelStarsRoute.Env
import ua.kpi.ipsa.route.syntax._
import ua.kpi.ipsa.route.{AppExceptionHandler, HotelStarsRoute}
import ua.kpi.ipsa.service.HotelStarsServiceLive
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.{App, ExitCode, Task, URIO, ZEnv, ZIO}

import java.util.UUID

object MainApp extends App {
  implicit val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    appProgramResource.useForever.provideLayer(appLayer).exitCode

  val appLayer = ZEnv.live >+>
    AppConfig.live >+>
    DBLayer.live >+>
    HotelStarsRepositoryLive.layer >+>
    HotelStarsServiceLive.layer >+>
    EventLoopGroup.auto(2) >+>
    ServerChannelFactory.auto

  PrometheusMetrics[Task]("tapir", CollectorRegistry.defaultRegistry)
    .withRequestsTotal()
    .withResponsesTotal()
  private val serverOptions: ZioHttpServerOptions[Env] = ZioHttpServerOptions
    .customInterceptors[Env]
    .exceptionHandler(AppExceptionHandler.handler)
//    .serverLog()
    //    .metricsInterceptor(prometheusMetrics.metricsInterceptor())
    .options
    .prependXRequestIdInterceptor(() => URIO(UUID.randomUUID().toString))

  val appProgramResource = {
    for {
      port <- ZIO.service[AppConfig].map(_.server.port).toManaged_
      res <- (Server.port(port) ++ Server.app {
               val interpreter = ZioHttpInterpreter(serverOptions)
               val openApiDocs = OpenAPIDocsInterpreter().toOpenAPI(HotelStarsRoute.swaggerEndpoints, "Swagger docs", "1.0.0")
               HotelStarsRoute.endpoints(interpreter) +++ new SwaggerZioHttp(openApiDocs.toYaml).route
             }).make
    } yield res
  }
}
