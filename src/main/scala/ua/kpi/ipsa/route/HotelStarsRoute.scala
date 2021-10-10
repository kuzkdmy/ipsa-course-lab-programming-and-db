package ua.kpi.ipsa.route

import sttp.model.StatusCode
import sttp.tapir
import sttp.tapir._
import sttp.tapir.codec.newtype._
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ua.kpi.ipsa.domain.types.{HotelStarCategoryId, QueryLimit}
import ua.kpi.ipsa.dto.{ApiCreateHotelStarCategory, ApiHotelStarCategory, ApiUpdateHotelStarCategory}
import ua.kpi.ipsa.route.middleware.syntax._
import ua.kpi.ipsa.service.HotelStarsService
import ua.kpi.ipsa.service.convert.HotelStarConverter._
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{Endpoint => _, HttpApp}
import zio._

object HotelStarsRoute {
  type Env = Has[HotelStarsRouteService]

  def endpoints[R <: Env](interpreter: ZioHttpInterpreter[R]): HttpApp[R, Throwable] =
    interpreter.toHttp(listE) { i => HotelStarsRouteService(_.list(i)) } <>
      interpreter.toHttp(getE) { i => HotelStarsRouteService(_.get(i)) } <>
      interpreter.toHttp(deleteE) { i => HotelStarsRouteService(_.delete(i)) } <>
      interpreter.toHttp(updateE) { i => HotelStarsRouteService(_.update(i)) } <>
      interpreter.toHttp(createE) { i => HotelStarsRouteService(_.create(i)) }

  lazy val swaggerEndpoints = List(getE, deleteE, updateE, createE, listE)

  private val getE: Endpoint[(HotelStarCategoryId, Ctx), NotFound, ApiHotelStarCategory, Any] = endpoint.get
    .in("api" / "v1.0" / "hotel_stars" / path[HotelStarCategoryId]("id"))
    .out(jsonBody[ApiHotelStarCategory])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val listE: Endpoint[(List[HotelStarCategoryId], Option[QueryLimit], Ctx), Unit, List[ApiHotelStarCategory], Any] = endpoint.get
    .in("api" / "v1.0" / "hotel_stars")
    .out(jsonBody[List[ApiHotelStarCategory]])
    .in(query[List[HotelStarCategoryId]]("ids"))
    .in(query[Option[QueryLimit]]("limit"))
    .withRequestContext()
  private val deleteE: Endpoint[(HotelStarCategoryId, Ctx), NotFound, Unit, Any] = endpoint.delete
    .in("api" / "v1.0" / "hotel_stars" / path[HotelStarCategoryId]("id"))
    .out(emptyOutput)
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val updateE: Endpoint[(HotelStarCategoryId, ApiUpdateHotelStarCategory, Ctx), NotFound, ApiHotelStarCategory, Any] = endpoint.put
    .in("api" / "v1.0" / "hotel_stars" / path[HotelStarCategoryId]("id"))
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

trait HotelStarsRouteService {
  def create(input: (ApiCreateHotelStarCategory, Ctx)): Task[Either[Conflict, ApiHotelStarCategory]]
  def get(input: (HotelStarCategoryId, Ctx)): Task[Either[NotFound, ApiHotelStarCategory]]
  def list(input: (List[HotelStarCategoryId], Option[QueryLimit], Ctx)): Task[Either[Unit, List[ApiHotelStarCategory]]]
  def delete(input: (HotelStarCategoryId, Ctx)): Task[Either[NotFound, Unit]]
  def update(input: (HotelStarCategoryId, ApiUpdateHotelStarCategory, Ctx)): Task[Either[NotFound, ApiHotelStarCategory]]
}
object HotelStarsRouteService extends Accessible[HotelStarsRouteService]
class HotelStarsRouteServiceLive(service: HotelStarsService) extends HotelStarsRouteService {
  override def create(input: (ApiCreateHotelStarCategory, Ctx)): Task[Either[Conflict, ApiHotelStarCategory]] = {
    val (apiCmd, ctx) = input
    for {
      domainCmd <- ZIO.succeed(toCreateHotelStarCategory(apiCmd))
      res       <- service.create(domainCmd)(ctx)
    } yield Right(toApiHotelStarCategory(res))
  }
  override def get(input: (HotelStarCategoryId, Ctx)): Task[Either[NotFound, ApiHotelStarCategory]] = {
    val (id, ctx) = input
    for {
      resOpt <- service.get(id.value)(ctx)
    } yield resOpt match {
      case Some(r) => Right(toApiHotelStarCategory(r))
      case None    => Left(notFound(id))
    }
  }
  override def list(input: (List[HotelStarCategoryId], Option[QueryLimit], Ctx)): Task[Either[Unit, List[ApiHotelStarCategory]]] = {
    val (ids, queryLimit, ctx) = input
    for {
      res <- service.list(toListHotelStarCategoryFilter(ids, queryLimit))(ctx)
    } yield Right(res.map(r => toApiHotelStarCategory(r)))
  }
  override def delete(input: (HotelStarCategoryId, Ctx)): Task[Either[NotFound, Unit]] = {
    val (id, ctx) = input
    for {
      deleteOpt <- service.delete(id.value)(ctx)
    } yield deleteOpt match {
      case Some(_) => Right(())
      case None    => Left(notFound(id))
    }
  }
  override def update(input: (HotelStarCategoryId, ApiUpdateHotelStarCategory, Ctx)): Task[Either[NotFound, ApiHotelStarCategory]] = {
    val (id, cmd, ctx) = input
    for {
      domainCmd <- ZIO.succeed(toUpdateHotelStarCategory(id, cmd))
      updateOpt <- service.update(domainCmd)(ctx)
    } yield updateOpt match {
      case Some(r) => Right(toApiHotelStarCategory(r))
      case None    => Left(notFound(id))
    }
  }
  private def notFound(id: HotelStarCategoryId) = NotFound(s"hotel star category id:$id not found")
}
object HotelStarsRouteServiceLive {
  val layer = ZLayer.fromService[HotelStarsService, HotelStarsRouteService](new HotelStarsRouteServiceLive(_))
}
