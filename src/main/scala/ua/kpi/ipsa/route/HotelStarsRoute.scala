package ua.kpi.ipsa.route

import sttp.model.StatusCode
import sttp.tapir
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ua.kpi.ipsa.domain.HotelStarCategory
import ua.kpi.ipsa.dto.{ApiCreateHotelStarCategory, ApiHotelStarCategory, ApiUpdateHotelStarCategory}
import ua.kpi.ipsa.route.ApiConverter.toApiHotelStarCategory
import ua.kpi.ipsa.route.syntax.ctx
import ua.kpi.ipsa.service.HotelStarsService
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{Endpoint => _, HttpApp}
import zio._

object HotelStarsRoute {
  type Env = Has[HotelStarsService]

  def endpoints(interpreter: ZioHttpInterpreter[Env]): HttpApp[Env, Throwable] =
    interpreter.toHttp(listE)(ctx => list()(ctx)) <>
      interpreter.toHttp(getE) { case (id, ctx) => get(id)(ctx) } <>
      interpreter.toHttp(deleteE) { case (id, ctx) => delete(id)(ctx) } <>
      interpreter.toHttp(updateE) { case (id, cmd, ctx) => update(id, cmd)(ctx) } <>
      interpreter.toHttp(createE) { case (cmd, ctx) => create(cmd)(ctx) }

  lazy val swaggerEndpoints = List(getE, deleteE, updateE, createE, listE)

  private val getE: Endpoint[(Int, Ctx), NotFound, ApiHotelStarCategory, Any] = endpoint.get
    .in("api" / "v1.0" / "hotel_stars" / path[Int]("id"))
    .out(jsonBody[ApiHotelStarCategory])
    .in(extractFromRequest[Ctx](ctx))
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val listE: Endpoint[Ctx, Unit, List[ApiHotelStarCategory], Any] = endpoint.get
    .in("api" / "v1.0" / "hotel_stars")
    .out(jsonBody[List[ApiHotelStarCategory]])
    .in(extractFromRequest[Ctx](ctx))
  private val deleteE: Endpoint[(Int, Ctx), NotFound, Unit, Any] = endpoint.delete
    .in("api" / "v1.0" / "hotel_stars" / path[Int]("id"))
    .out(emptyOutput)
    .in(extractFromRequest[Ctx](ctx))
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val updateE: Endpoint[(Int, ApiUpdateHotelStarCategory, Ctx), NotFound, ApiHotelStarCategory, Any] = endpoint.put
    .in("api" / "v1.0" / "hotel_stars" / path[Int]("id"))
    .in(jsonBody[ApiUpdateHotelStarCategory])
    .out(jsonBody[ApiHotelStarCategory])
    .in(extractFromRequest[Ctx](ctx))
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val createE: Endpoint[(ApiCreateHotelStarCategory, Ctx), Conflict, ApiHotelStarCategory, Any] = endpoint.post
    .in("api" / "v1.0" / "hotel_stars")
    .in(jsonBody[ApiCreateHotelStarCategory])
    .out(jsonBody[ApiHotelStarCategory])
    .in(extractFromRequest[Ctx](ctx))
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[Conflict].description("conflict"))
      )
    )

  private def get(id: Int)(implicit ctx: Ctx): RIO[Env, Either[NotFound, ApiHotelStarCategory]] = {
    HotelStarsService(_.get(id))
      .map {
        case Some(r) => Right(toApiHotelStarCategory(r))
        case None    => Left(notFound(id))
      }
  }

  private def delete(id: Int)(implicit ctx: Ctx): RIO[Env, Either[NotFound, Unit]] = {
    HotelStarsService(_.delete(id))
      .map(deleteCount => if (deleteCount < 1) Left(notFound(id)) else Right(()))
  }
  private def update(id: Int, cmd: ApiUpdateHotelStarCategory)(implicit ctx: Ctx): RIO[Env, Either[NotFound, ApiHotelStarCategory]] = {
    HotelStarsService(_.update(HotelStarCategory(id, cmd.stars, cmd.description, cmd.region)).map {
      case Some(r) => Right(toApiHotelStarCategory(r))
      case None    => Left(notFound(id))
    })
  }
  private def create(cmd: ApiCreateHotelStarCategory)(implicit ctx: Ctx): RIO[Env, Either[Conflict, ApiHotelStarCategory]] = {
    HotelStarsService(_.create(HotelStarCategory(-1, cmd.stars, cmd.description, cmd.region)).map { r => Right(toApiHotelStarCategory(r)) })
  }
  private def list()(implicit ctx: Ctx): RIO[Env, Either[Unit, List[ApiHotelStarCategory]]] = {
    HotelStarsService(_.list()).map(list => Right(list.map(r => toApiHotelStarCategory(r))))
  }
  private def notFound(id: Int) = NotFound(s"hotel star category id:$id not found")
}
