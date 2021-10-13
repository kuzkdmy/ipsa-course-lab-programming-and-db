package ua.kpi.ipsa.route

import sttp.model.StatusCode
import sttp.tapir
import sttp.tapir._
import sttp.tapir.codec.newtype._
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ua.kpi.ipsa.domain.types._
import ua.kpi.ipsa.dto.{ApiCreateTravelVoucher, ApiTravelVoucher, ApiUpdateTravelVoucher}
import ua.kpi.ipsa.route.TravelVoucherRoute.ListQueryParams
import ua.kpi.ipsa.route.middleware.syntax._
import ua.kpi.ipsa.service.TravelVoucherService
import ua.kpi.ipsa.service.convert.TravelVoucherConverter
import ua.kpi.ipsa.trace.Ctx
import zhttp.http.{HttpApp, Endpoint => _}
import zio._

object TravelVoucherRoute {
  type Env = Has[TravelVoucherRouteService]

  def endpoints[R <: Env](interpreter: ZioHttpInterpreter[R]): HttpApp[R, Throwable] =
    interpreter.toHttp(listE) { i => TravelVoucherRouteService(_.list(i)) } <>
      interpreter.toHttp(getE) { i => TravelVoucherRouteService(_.get(i)) } <>
      interpreter.toHttp(deleteE) { i => TravelVoucherRouteService(_.delete(i)) } <>
      interpreter.toHttp(updateE) { i => TravelVoucherRouteService(_.update(i)) } <>
      interpreter.toHttp(createE) { i => TravelVoucherRouteService(_.create(i)) }

  lazy val swaggerEndpoints = List(getE, deleteE, updateE, createE, listE)

  private val getE: Endpoint[(TravelVoucherId, Ctx), NotFound, ApiTravelVoucher, Any] = endpoint.get
    .in("api" / "v1.0" / "travel_voucher" / path[TravelVoucherId]("id"))
    .out(jsonBody[ApiTravelVoucher])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  // format: off
  type ListQueryParams = (
      List[TravelVoucherId], List[TravelAgentId],
      List[TravelVoucherCreatedDay], Option[TravelVoucherCreatedDay], Option[TravelVoucherCreatedDay],
      List[TravelVoucherUpdatedDay], Option[TravelVoucherUpdatedDay], Option[TravelVoucherUpdatedDay],
      List[TravelVoucherStartDay], Option[TravelVoucherStartDay], Option[TravelVoucherStartDay],
      List[TravelVoucherEndDay], Option[TravelVoucherEndDay], Option[TravelVoucherEndDay],
      List[UserLastName], List[UserPassportNumber], Option[QueryLimit],
      Ctx
  )
  // format: on

  private val listE: Endpoint[ListQueryParams, Unit, List[ApiTravelVoucher], Any] = endpoint.get
    .in("api" / "v1.0" / "travel_voucher")
    .out(jsonBody[List[ApiTravelVoucher]])
    .in(query[List[TravelVoucherId]]("id"))
    .in(query[List[TravelAgentId]]("agent_id"))
    .in(query[List[TravelVoucherCreatedDay]]("created_day"))
    .in(query[Option[TravelVoucherCreatedDay]]("created_day_ge"))
    .in(query[Option[TravelVoucherCreatedDay]]("created_day_lt"))
    .in(query[List[TravelVoucherUpdatedDay]]("updated_day"))
    .in(query[Option[TravelVoucherUpdatedDay]]("updated_day_ge"))
    .in(query[Option[TravelVoucherUpdatedDay]]("updated_day_lt"))
    .in(query[List[TravelVoucherStartDay]]("start_day"))
    .in(query[Option[TravelVoucherStartDay]]("start_day_ge"))
    .in(query[Option[TravelVoucherStartDay]]("start_day_le"))
    .in(query[List[TravelVoucherEndDay]]("end_day"))
    .in(query[Option[TravelVoucherEndDay]]("end_day_ge"))
    .in(query[Option[TravelVoucherEndDay]]("end_day_le"))
    .in(query[List[UserLastName]]("user_last_name"))
    .in(query[List[UserPassportNumber]]("user_passport"))
    .in(query[Option[QueryLimit]]("limit"))
    .withRequestContext()
  private val deleteE: Endpoint[(TravelVoucherId, Ctx), NotFound, Unit, Any] = endpoint.delete
    .in("api" / "v1.0" / "travel_voucher" / path[TravelVoucherId]("id"))
    .out(emptyOutput)
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val updateE: Endpoint[(TravelVoucherId, ApiUpdateTravelVoucher, Ctx), NotFound, ApiTravelVoucher, Any] = endpoint.put
    .in("api" / "v1.0" / "travel_voucher" / path[TravelVoucherId]("id"))
    .in(jsonBody[ApiUpdateTravelVoucher])
    .out(jsonBody[ApiTravelVoucher])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[NotFound].description("not found"))
      )
    )
  private val createE: Endpoint[(ApiCreateTravelVoucher, Ctx), Conflict, ApiTravelVoucher, Any] = endpoint.post
    .in("api" / "v1.0" / "travel_voucher")
    .in(jsonBody[ApiCreateTravelVoucher])
    .out(jsonBody[ApiTravelVoucher])
    .withRequestContext()
    .errorOut(
      tapir.oneOf(
        oneOfMappingFromMatchType(StatusCode.NotFound, jsonBody[Conflict].description("conflict"))
      )
    )
}

trait TravelVoucherRouteService {
  def create(input: (ApiCreateTravelVoucher, Ctx)): Task[Either[Conflict, ApiTravelVoucher]]
  def get(input: (TravelVoucherId, Ctx)): Task[Either[NotFound, ApiTravelVoucher]]
  def list(input: ListQueryParams): Task[Either[Unit, List[ApiTravelVoucher]]]
  def delete(input: (TravelVoucherId, Ctx)): Task[Either[NotFound, Unit]]
  def update(input: (TravelVoucherId, ApiUpdateTravelVoucher, Ctx)): Task[Either[NotFound, ApiTravelVoucher]]
}
object TravelVoucherRouteService extends Accessible[TravelVoucherRouteService]
class TravelVoucherRouteServiceLive(service: TravelVoucherService) extends TravelVoucherRouteService {
  override def create(input: (ApiCreateTravelVoucher, Ctx)): Task[Either[Conflict, ApiTravelVoucher]] = {
    val (apiCmd, ctx) = input
    for {
      domainCmd <- ZIO.succeed(TravelVoucherConverter.toCreateTravelVoucher(apiCmd))
      res       <- service.create(domainCmd)(ctx)
    } yield Right(TravelVoucherConverter.toApiTravelVoucher(res))
  }
  override def get(input: (TravelVoucherId, Ctx)): Task[Either[NotFound, ApiTravelVoucher]] = {
    val (id, ctx) = input
    for {
      getOpt <- service.get(id.value)(ctx)
    } yield getOpt match {
      case Some(r) => Right(TravelVoucherConverter.toApiTravelVoucher(r))
      case None    => Left(notFound(id))
    }
  }
  // format: off
  override def list(input: ListQueryParams): Task[Either[Unit, List[ApiTravelVoucher]]] = {
    val (
      ids      , agents, 
      createdAt, createdAtGE, createdAtLT, 
      updatedAt, updatedAtGE, updatedAtLT, 
      startAt  , startAtGE  , startAtLT  , 
      endAt    , endAtGE    , endAtLT    ,
      lastNames, passports  , queryLimit , ctx
    ) = input
    for {
      filter <- ZIO.succeed(TravelVoucherConverter.toListTravelVoucherFilter(ids      , agents,
        createdAt, createdAtGE, createdAtLT,
        updatedAt, updatedAtGE, updatedAtLT,
        startAt  , startAtGE  , startAtLT  ,
        endAt    , endAtGE    , endAtLT    ,
        lastNames, passports  , queryLimit))
      res    <- service.list(filter)(ctx)
    } yield Right(res.map(TravelVoucherConverter.toApiTravelVoucher))
  }
  // format: on
  override def delete(input: (TravelVoucherId, Ctx)): Task[Either[NotFound, Unit]] = {
    val (id, ctx) = input
    for {
      deleteOpt <- service.delete(id.value)(ctx)
    } yield deleteOpt match {
      case Some(_) => Right(())
      case None    => Left(notFound(id))
    }
  }
  override def update(input: (TravelVoucherId, ApiUpdateTravelVoucher, Ctx)): Task[Either[NotFound, ApiTravelVoucher]] = {
    val (id, cmd, ctx) = input
    for {
      domainCmd <- ZIO.succeed(TravelVoucherConverter.toUpdateTravelVoucher(id, cmd))
      updateOpt <- service.update(domainCmd)(ctx)
    } yield updateOpt match {
      case Some(r) => Right(TravelVoucherConverter.toApiTravelVoucher(r))
      case None    => Left(notFound(id))
    }
  }
  private def notFound(id: TravelVoucherId) = NotFound(s"TravelVoucher id:$id not found")
}
object TravelVoucherRouteServiceLive {
  val layer = ZLayer.fromService[TravelVoucherService, TravelVoucherRouteService](new TravelVoucherRouteServiceLive(_))
}
