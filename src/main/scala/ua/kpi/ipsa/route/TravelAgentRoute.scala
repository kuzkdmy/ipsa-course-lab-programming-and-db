package ua.kpi.ipsa.route

import sttp.model.StatusCode
import sttp.tapir
import sttp.tapir._
import sttp.tapir.codec.newtype._
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ua.kpi.ipsa.domain.types._
import ua.kpi.ipsa.dto._
import ua.kpi.ipsa.route.TravelAgentRoute.ListQueryParams
import ua.kpi.ipsa.route.middleware.syntax._
import ua.kpi.ipsa.service.TravelAgentService
import ua.kpi.ipsa.service.convert.TravelAgentConverter._
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{HttpApp, Endpoint => _}
import zio._

object TravelAgentRoute {
  type Env = Has[TravelAgentRouteService]

  def endpoints[R <: Env](interpreter: ZioHttpInterpreter[R]): HttpApp[R, Throwable] =
    interpreter.toHttp(listE) { i => TravelAgentRouteService(_.list(i)) } <>
      interpreter.toHttp(getE) { i => TravelAgentRouteService(_.get(i)) } <>
      interpreter.toHttp(deleteE) { i => TravelAgentRouteService(_.delete(i)) } <>
      interpreter.toHttp(updateE) { i => TravelAgentRouteService(_.update(i)) } <>
      interpreter.toHttp(createE) { i => TravelAgentRouteService(_.create(i)) }

  lazy val swaggerEndpoints = List(getE, deleteE, updateE, createE, listE)

  private val getE: Endpoint[(TravelAgentId, Ctx), NotFound, ApiTravelAgent, Any] = endpoint.get
    .in("api" / "v1.0" / "travel_agent" / path[TravelAgentId]("id"))
    .out(jsonBody[ApiTravelAgent])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  type ListQueryParams = (List[TravelAgentId], List[TravelAgentName], List[TravelAgentCountry], List[TravelAgentCity], List[TravelAgentPhoto], List[HotelStars], Option[QueryLimit], Ctx)
  private val listE: Endpoint[ListQueryParams, Unit, List[ApiTravelAgent], Any] =
    endpoint.get
      .in("api" / "v1.0" / "travel_agent")
      .out(jsonBody[List[ApiTravelAgent]])
      .in(query[List[TravelAgentId]]("id"))
      .in(query[List[TravelAgentName]]("name"))
      .in(query[List[TravelAgentCountry]]("country"))
      .in(query[List[TravelAgentCity]]("city"))
      .in(query[List[TravelAgentPhoto]]("photo"))
      .in(query[List[HotelStars]]("stars"))
      .in(query[Option[QueryLimit]]("limit"))
      .withRequestContext()

  private val deleteE: Endpoint[(TravelAgentId, Ctx), NotFound, Unit, Any] = endpoint.delete
    .in("api" / "v1.0" / "travel_agent" / path[TravelAgentId]("id"))
    .out(emptyOutput)
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val updateE: Endpoint[(TravelAgentId, ApiUpdateTravelAgent, Ctx), NotFound, ApiTravelAgent, Any] = endpoint.put
    .in("api" / "v1.0" / "travel_agent" / path[TravelAgentId]("id"))
    .in(jsonBody[ApiUpdateTravelAgent])
    .out(jsonBody[ApiTravelAgent])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val createE: Endpoint[(ApiCreateTravelAgent, Ctx), Conflict, ApiTravelAgent, Any] = endpoint.post
    .in("api" / "v1.0" / "travel_agent")
    .in(jsonBody[ApiCreateTravelAgent])
    .out(jsonBody[ApiTravelAgent])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[Conflict].description("conflict"))
      )
    )
}

trait TravelAgentRouteService {
  def create(input: (ApiCreateTravelAgent, Ctx)): Task[Either[Conflict, ApiTravelAgent]]
  def get(input: (TravelAgentId, Ctx)): Task[Either[NotFound, ApiTravelAgent]]
  def list(input: ListQueryParams): Task[Either[Unit, List[ApiTravelAgent]]]
  def delete(input: (TravelAgentId, Ctx)): Task[Either[NotFound, Unit]]
  def update(input: (TravelAgentId, ApiUpdateTravelAgent, Ctx)): Task[Either[NotFound, ApiTravelAgent]]
}
object TravelAgentRouteService extends Accessible[TravelAgentRouteService]
class TravelAgentRouteServiceLive(service: TravelAgentService) extends TravelAgentRouteService {
  override def create(input: (ApiCreateTravelAgent, Ctx)): Task[Either[Conflict, ApiTravelAgent]] = {
    val (apiCmd, ctx) = input
    for {
      domainCmd <- ZIO.succeed(toCreateTravelAgent(apiCmd))
      res       <- service.create(domainCmd)(ctx)
    } yield Right(toApiTravelAgent(res))
  }
  override def get(input: (TravelAgentId, Ctx)): Task[Either[NotFound, ApiTravelAgent]] = {
    val (id, ctx) = input
    for {
      getOpt <- service.get(id.value)(ctx)
    } yield getOpt match {
      case Some(r) => Right(toApiTravelAgent(r))
      case None    => Left(notFound(id))
    }
  }
  override def list(input: ListQueryParams): Task[Either[Unit, List[ApiTravelAgent]]] = {
    val (ids, namesIn, countriesIn, citiesIn, photosIn, hotelStarsIn, queryLimit, ctx) = input
    for {
      filter <- ZIO.succeed(toListTravelAgentFilter(ids, namesIn, countriesIn, citiesIn, photosIn, hotelStarsIn, queryLimit))
      res    <- service.list(filter)(ctx)
    } yield Right(res.map(toApiTravelAgent))
  }
  override def delete(input: (TravelAgentId, Ctx)): Task[Either[NotFound, Unit]] = {
    val (id, ctx) = input
    for {
      deleteOpt <- service.delete(id.value)(ctx)
    } yield deleteOpt match {
      case Some(_) => Right(())
      case None    => Left(notFound(id))
    }
  }
  override def update(input: (TravelAgentId, ApiUpdateTravelAgent, Ctx)): Task[Either[NotFound, ApiTravelAgent]] = {
    val (id, cmd, ctx) = input
    for {
      domainCmd <- ZIO.succeed(toUpdateTravelAgent(id, cmd))
      updateOpt <- service.update(domainCmd)(ctx)
    } yield updateOpt match {
      case Some(r) => Right(toApiTravelAgent(r))
      case None    => Left(notFound(id))
    }
  }
  private def notFound(id: TravelAgentId) = NotFound(s"travel agent id:$id not found")
}
object TravelAgentRouteServiceLive {
  val layer = ZLayer.fromService[TravelAgentService, TravelAgentRouteService](new TravelAgentRouteServiceLive(_))
}
