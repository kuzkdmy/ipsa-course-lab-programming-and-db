package ua.kpi.ipsa.route

import sttp.model.StatusCode
import sttp.tapir
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ua.kpi.ipsa.dto.filter.ApiListTravelAgentsFilter
import ua.kpi.ipsa.dto.{ApiCreateTravelAgent, ApiTravelAgent, ApiUpdateTravelAgent}
import ua.kpi.ipsa.route.middleware.syntax._
import ua.kpi.ipsa.service.TravelAgentService
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{HttpApp, Endpoint => _}
import zio._

object TravelAgentRoute {
  type Env = Has[TravelAgentService]

  def endpoints[R <: Env](interpreter: ZioHttpInterpreter[R]): HttpApp[R, Throwable] =
    interpreter.toHttp(listE) { case (ids, limit, ctx) => TravelAgentService(_.list(ApiListTravelAgentsFilter(ids, limit))(ctx)) } <>
      interpreter.toHttp(getE) { case (id, ctx) => TravelAgentService(_.get(id)(ctx)) } <>
      interpreter.toHttp(deleteE) { case (id, ctx) => TravelAgentService(_.delete(id)(ctx)) } <>
      interpreter.toHttp(updateE) { case (id, cmd, ctx) => TravelAgentService(_.update(id, cmd)(ctx)) } <>
      interpreter.toHttp(createE) { case (cmd, ctx) => TravelAgentService(_.create(cmd)(ctx)) }

  lazy val swaggerEndpoints = List(getE, deleteE, updateE, createE, listE)

  private val getE: Endpoint[(Long, Ctx), NotFound, ApiTravelAgent, Any] = endpoint.get
    .in("api" / "v1.0" / "travel_agent" / path[Long]("id"))
    .out(jsonBody[ApiTravelAgent])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val listE: Endpoint[(List[Long], Option[Int], Ctx), Unit, List[ApiTravelAgent], Any] = endpoint.get
    .in("api" / "v1.0" / "travel_agent")
    .out(jsonBody[List[ApiTravelAgent]])
    .in(query[List[Long]]("ids"))
    .in(query[Option[Int]]("limit"))
    .withRequestContext()
  private val deleteE: Endpoint[(Long, Ctx), NotFound, Unit, Any] = endpoint.delete
    .in("api" / "v1.0" / "travel_agent" / path[Long]("id"))
    .out(emptyOutput)
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val updateE: Endpoint[(Long, ApiUpdateTravelAgent, Ctx), NotFound, ApiTravelAgent, Any] = endpoint.put
    .in("api" / "v1.0" / "travel_agent" / path[Long]("id"))
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
