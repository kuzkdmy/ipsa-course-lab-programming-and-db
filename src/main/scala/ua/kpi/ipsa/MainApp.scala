package ua.kpi.ipsa

import org.slf4j.LoggerFactory
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.ziohttp.SwaggerZioHttp
import ua.kpi.ipsa.config.AppConfig
import ua.kpi.ipsa.repository.{DBLayer, HotelStarsRepositoryLive, LocationRepositoryLive, TravelAgentRepositoryLive, TravelVoucherRepositoryLive}
import ua.kpi.ipsa.route.middleware.{ServerOptionsService, ServerOptionsServiceLive}
import ua.kpi.ipsa.route._
import ua.kpi.ipsa.service.{HotelStarsServiceLive, LocationServiceLive, TravelAgentServiceLive, TravelVoucherServiceLive}
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.metrics.prometheus.Registry
import zio.{App, ExitCode, URIO, ZEnv, ZIO}

object MainApp extends App {
  implicit val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    appProgramResource.useForever.provideLayer(appLayer).exitCode

  // format: off
  val appLayer =
    // common layers
    ZEnv.live >+> AppConfig.live >+> Registry.live >+>
    // db layers
    DBLayer.live >+> LocationRepositoryLive.layer >+> HotelStarsRepositoryLive.layer >+> TravelAgentRepositoryLive.layer >+> TravelVoucherRepositoryLive.layer >+>
    // service layers
    LocationServiceLive.layer >+> HotelStarsServiceLive.layer >+> TravelAgentServiceLive.layer >+> TravelVoucherServiceLive.layer >+>
    // routes access service layers
    LocationRouteServiceLive.layer >+> HotelStarsRouteServiceLive.layer >+> TravelAgentRouteServiceLive.layer >+> TravelVoucherRouteServiceLive.layer >+>
    // zio-http layers
    ServerOptionsServiceLive.layer >+> EventLoopGroup.auto() >+> ServerChannelFactory.auto
  // format: on

  private type RouteEnv = LocationRoute.Env with HotelStarsRoute.Env with TravelAgentRoute.Env with TravelVoucherRoute.Env
  val appProgramResource = for {
    port      <- ZIO.service[AppConfig].map(_.server.port).toManaged_
    serverOps <- ZIO.service[ServerOptionsService].flatMap(_.serverOptions[RouteEnv]).toManaged_
    res <- (Server.port(port) ++ Server.app {
             val interpreter      = ZioHttpInterpreter(serverOps)
             val swaggerEndpoints = LocationRoute.swaggerEndpoints ++ HotelStarsRoute.swaggerEndpoints ++ TravelAgentRoute.swaggerEndpoints ++ TravelVoucherRoute.swaggerEndpoints
             val openApiDocs      = OpenAPIDocsInterpreter().toOpenAPI(swaggerEndpoints, "Swagger docs", "1.0.0")
             LocationRoute.endpoints(interpreter) <>
               HotelStarsRoute.endpoints(interpreter) <>
               TravelAgentRoute.endpoints(interpreter) <>
               TravelVoucherRoute.endpoints(interpreter) <>
               new SwaggerZioHttp(openApiDocs.toYaml).route
           }).make
  } yield res
}
