package ua.kpi.ipsa.route

import sttp.model.StatusCode
import sttp.tapir
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ua.kpi.ipsa.dto.filter.ApiListLocationsFilter
import ua.kpi.ipsa.dto.{ApiCreateLocation, ApiLocation, ApiUpdateLocation}
import ua.kpi.ipsa.route.middleware.syntax._
import ua.kpi.ipsa.service.{ApiConverter, LocationService}
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{Endpoint => _, HttpApp}
import zio._

object LocationRoute {
  type Env = Has[LocationService]

  def endpoints[R <: Env](interpreter: ZioHttpInterpreter[R]): HttpApp[R, Throwable] =
    interpreter.toHttp(listE) { case (ids, limit, ctx) => LocationService(_.list(ApiListLocationsFilter(ids, limit))(ctx)) } <>
      interpreter.toHttp(getE) { case (id, ctx) => LocationService(_.get(id)(ctx)) } <>
      interpreter.toHttp(deleteE) { case (id, ctx) => LocationService(_.delete(id)(ctx)) } <>
      interpreter.toHttp(updateE) { case (id, cmd, ctx) => LocationService(_.update(ApiConverter.toUpdateLocation(id, cmd))(ctx)) } <>
      interpreter.toHttp(createE) { case (cmd, ctx) => LocationService(_.create(ApiConverter.toCreateLocation(cmd))(ctx)) }

  lazy val swaggerEndpoints = List(getE, deleteE, updateE, createE, listE)

  private val getE: Endpoint[(Long, Ctx), NotFound, ApiLocation, Any] = endpoint.get
    .in("api" / "v1.0" / "location" / path[Long]("id"))
    .out(jsonBody[ApiLocation])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val listE: Endpoint[(List[Long], Option[Int], Ctx), Unit, List[ApiLocation], Any] = endpoint.get
    .in("api" / "v1.0" / "location")
    .out(jsonBody[List[ApiLocation]])
    .in(query[List[Long]]("ids"))
    .in(query[Option[Int]]("limit"))
    .withRequestContext()
  private val deleteE: Endpoint[(Long, Ctx), NotFound, Unit, Any] = endpoint.delete
    .in("api" / "v1.0" / "location" / path[Long]("id"))
    .out(emptyOutput)
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val updateE: Endpoint[(Long, ApiUpdateLocation, Ctx), NotFound, ApiLocation, Any] = endpoint.put
    .in("api" / "v1.0" / "location" / path[Long]("id"))
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
