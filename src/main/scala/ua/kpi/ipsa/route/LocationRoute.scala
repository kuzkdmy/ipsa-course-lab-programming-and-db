package ua.kpi.ipsa.route

import sttp.model.StatusCode
import sttp.tapir
import sttp.tapir._
import sttp.tapir.codec.newtype._
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ua.kpi.ipsa.domain.types.{LocationId, QueryLimit}
import ua.kpi.ipsa.dto.{ApiCreateLocation, ApiLocation, ApiUpdateLocation}
import ua.kpi.ipsa.route.middleware.syntax._
import ua.kpi.ipsa.service.LocationService
import ua.kpi.ipsa.service.convert.LocationConverter
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{Endpoint => _, HttpApp}
import zio._

object LocationRoute {
  type Env = Has[LocationRouteService]

  def endpoints[R <: Env](interpreter: ZioHttpInterpreter[R]): HttpApp[R, Throwable] =
    interpreter.toHttp(listE) { i => LocationRouteService(_.list(i)) } <>
      interpreter.toHttp(getE) { i => LocationRouteService(_.get(i)) } <>
      interpreter.toHttp(deleteE) { i => LocationRouteService(_.delete(i)) } <>
      interpreter.toHttp(updateE) { i => LocationRouteService(_.update(i)) } <>
      interpreter.toHttp(createE) { i => LocationRouteService(_.create(i)) }

  lazy val swaggerEndpoints = List(getE, deleteE, updateE, createE, listE)

  private val getE: Endpoint[(LocationId, Ctx), NotFound, ApiLocation, Any] = endpoint.get
    .in("api" / "v1.0" / "location" / path[LocationId]("id"))
    .out(jsonBody[ApiLocation])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val listE: Endpoint[(List[LocationId], Option[QueryLimit], Ctx), Unit, List[ApiLocation], Any] = endpoint.get
    .in("api" / "v1.0" / "location")
    .out(jsonBody[List[ApiLocation]])
    .in(query[List[LocationId]]("id"))
    .in(query[Option[QueryLimit]]("limit"))
    .withRequestContext()
  private val deleteE: Endpoint[(LocationId, Ctx), NotFound, Unit, Any] = endpoint.delete
    .in("api" / "v1.0" / "location" / path[LocationId]("id"))
    .out(emptyOutput)
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val updateE: Endpoint[(LocationId, ApiUpdateLocation, Ctx), NotFound, ApiLocation, Any] = endpoint.put
    .in("api" / "v1.0" / "location" / path[LocationId]("id"))
    .in(jsonBody[ApiUpdateLocation])
    .out(jsonBody[ApiLocation])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val createE: Endpoint[(ApiCreateLocation, Ctx), Conflict, ApiLocation, Any] = endpoint.post
    .in("api" / "v1.0" / "location")
    .in(jsonBody[ApiCreateLocation])
    .out(jsonBody[ApiLocation])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[Conflict].description("conflict"))
      )
    )
}

trait LocationRouteService {
  def create(input: (ApiCreateLocation, Ctx)): Task[Either[Conflict, ApiLocation]]
  def get(input: (LocationId, Ctx)): Task[Either[NotFound, ApiLocation]]
  def list(input: (List[LocationId], Option[QueryLimit], Ctx)): Task[Either[Unit, List[ApiLocation]]]
  def delete(input: (LocationId, Ctx)): Task[Either[NotFound, Unit]]
  def update(input: (LocationId, ApiUpdateLocation, Ctx)): Task[Either[NotFound, ApiLocation]]
}
object LocationRouteService extends Accessible[LocationRouteService]
class LocationRouteServiceLive(service: LocationService) extends LocationRouteService {
  override def create(input: (ApiCreateLocation, Ctx)): Task[Either[Conflict, ApiLocation]] = {
    val (apiCmd, ctx) = input
    for {
      domainCmd <- ZIO.succeed(LocationConverter.toCreateLocation(apiCmd))
      res       <- service.create(domainCmd)(ctx)
    } yield Right(LocationConverter.toApiLocation(res))
  }
  override def get(input: (LocationId, Ctx)): Task[Either[NotFound, ApiLocation]] = {
    val (id, ctx) = input
    for {
      getOpt <- service.get(id.value)(ctx)
    } yield getOpt match {
      case Some(r) => Right(LocationConverter.toApiLocation(r))
      case None    => Left(notFound(id))
    }
  }
  override def list(input: (List[LocationId], Option[QueryLimit], Ctx)): Task[Either[Unit, List[ApiLocation]]] = {
    val (ids, queryLimit, ctx) = input
    for {
      filter <- ZIO.succeed(LocationConverter.toListLocationsFilter(ids, queryLimit))
      res    <- service.list(filter)(ctx)
    } yield Right(res.map(LocationConverter.toApiLocation))
  }
  override def delete(input: (LocationId, Ctx)): Task[Either[NotFound, Unit]] = {
    val (id, ctx) = input
    for {
      deleteOpt <- service.delete(id.value)(ctx)
    } yield deleteOpt match {
      case Some(_) => Right(())
      case None    => Left(notFound(id))
    }
  }
  override def update(input: (LocationId, ApiUpdateLocation, Ctx)): Task[Either[NotFound, ApiLocation]] = {
    val (id, cmd, ctx) = input
    for {
      domainCmd <- ZIO.succeed(LocationConverter.toUpdateLocation(id, cmd))
      updateOpt <- service.update(domainCmd)(ctx)
    } yield updateOpt match {
      case Some(r) => Right(LocationConverter.toApiLocation(r))
      case None    => Left(notFound(id))
    }
  }
  private def notFound(id: LocationId) = NotFound(s"location id:$id not found")
}
object LocationRouteServiceLive {
  val layer = ZLayer.fromService[LocationService, LocationRouteService](new LocationRouteServiceLive(_))
}
