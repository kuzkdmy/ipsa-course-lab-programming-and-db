package ua.kpi.ipsa.route

import sttp.model.StatusCode
import sttp.tapir
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ua.kpi.ipsa.dto.filter.ApiListHotelCategoriesFilter
import ua.kpi.ipsa.dto.{ApiCreateHotelStarCategory, ApiHotelStarCategory, ApiUpdateHotelStarCategory}
import ua.kpi.ipsa.route.middleware.syntax._
import ua.kpi.ipsa.service.HotelStarsService
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{Endpoint => _, HttpApp}
import zio._

object HotelStarsRoute {
  type Env = Has[HotelStarsService]

  def endpoints[R <: Env](interpreter: ZioHttpInterpreter[R]): HttpApp[R, Throwable] =
    interpreter.toHttp(listE) { case (ids, limit, ctx) => HotelStarsService(_.list(ApiListHotelCategoriesFilter(ids, limit))(ctx)) } <>
      interpreter.toHttp(getE) { case (id, ctx) => HotelStarsService(_.get(id)(ctx)) } <>
      interpreter.toHttp(deleteE) { case (id, ctx) => HotelStarsService(_.delete(id)(ctx)) } <>
      interpreter.toHttp(updateE) { case (id, cmd, ctx) => HotelStarsService(_.update(id, cmd)(ctx)) } <>
      interpreter.toHttp(createE) { case (cmd, ctx) => HotelStarsService(_.create(cmd)(ctx)) }

  lazy val swaggerEndpoints = List(getE, deleteE, updateE, createE, listE)

  private val getE: Endpoint[(Long, Ctx), NotFound, ApiHotelStarCategory, Any] = endpoint.get
    .in("api" / "v1.0" / "hotel_stars" / path[Long]("id"))
    .out(jsonBody[ApiHotelStarCategory])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val listE: Endpoint[(List[Long], Option[Int], Ctx), Unit, List[ApiHotelStarCategory], Any] = endpoint.get
    .in("api" / "v1.0" / "hotel_stars")
    .out(jsonBody[List[ApiHotelStarCategory]])
    .in(query[List[Long]]("ids"))
    .in(query[Option[Int]]("limit"))
    .withRequestContext()
  private val deleteE: Endpoint[(Long, Ctx), NotFound, Unit, Any] = endpoint.delete
    .in("api" / "v1.0" / "hotel_stars" / path[Long]("id"))
    .out(emptyOutput)
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val updateE: Endpoint[(Long, ApiUpdateHotelStarCategory, Ctx), NotFound, ApiHotelStarCategory, Any] = endpoint.put
    .in("api" / "v1.0" / "hotel_stars" / path[Long]("id"))
    .in(jsonBody[ApiUpdateHotelStarCategory])
    .out(jsonBody[ApiHotelStarCategory])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val createE: Endpoint[(ApiCreateHotelStarCategory, Ctx), Conflict, ApiHotelStarCategory, Any] = endpoint.post
    .in("api" / "v1.0" / "hotel_stars")
    .in(jsonBody[ApiCreateHotelStarCategory])
    .out(jsonBody[ApiHotelStarCategory])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[Conflict].description("conflict"))
      )
    )
}
