package ua.kpi.ipsa.service

import cats.data.NonEmptyList
import cats.syntax.list._
import cats.syntax.option._
import doobie.Transactor
import org.slf4j.LoggerFactory
import ua.kpi.ipsa.domain.filter.{ListHotelCategoriesFilter, ListLocationsFilter, ListTravelAgentFilter}
import ua.kpi.ipsa.domain.{HotelStarCategory, Location}
import ua.kpi.ipsa.dto.filter.ApiListTravelAgentsFilter
import ua.kpi.ipsa.dto.{ApiCreateTravelAgent, ApiTravelAgent, ApiUpdateTravelAgent}
import ua.kpi.ipsa.repository.{HotelStarsRepository, LocationRepository, TranzactIO, TravelAgentRepository}
import ua.kpi.ipsa.route.{Conflict, NotFound}
import ua.kpi.ipsa.trace.{Ctx, log}
import zio._

trait TravelAgentService {
  def create(cmd: ApiCreateTravelAgent)(implicit ctx: Ctx): Task[Either[Conflict, ApiTravelAgent]]
  def get(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, ApiTravelAgent]]
  def list(filter: ApiListTravelAgentsFilter)(implicit ctx: Ctx): Task[Either[Unit, List[ApiTravelAgent]]]
  def delete(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, Unit]]
  def update(id: Long, cmd: ApiUpdateTravelAgent)(implicit ctx: Ctx): Task[Either[NotFound, ApiTravelAgent]]
}
object TravelAgentService extends Accessible[TravelAgentService]

case class TravelAgentServiceLive(
    agentRepo: TravelAgentRepository,
    locationRepo: LocationRepository,
    categoriesRepo: HotelStarsRepository,
    tx: Transactor[Task]
) extends TravelAgentService {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def create(cmd: ApiCreateTravelAgent)(implicit ctx: Ctx): Task[Either[Conflict, ApiTravelAgent]] = {
    val program = for {
      _   <- log.info(s"call create travel agent $cmd")
      id  <- agentRepo.create(ApiConverter.toCreateTravelAgent(cmd))
      res <- queryList(ListTravelAgentFilter(ids = NonEmptyList.of(id).some))
    } yield res.headOption match {
      case Some(res) => Right(res)
      case None      => Left(Conflict(s"fail to query just created travel agent with id:$id"))
    }
    program.provideLayer(ZLayer.succeed(tx))
  }
  override def get(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, ApiTravelAgent]] = {
    val program = for {
      _   <- log.info(s"call get travel agent by id $id")
      res <- queryList(ListTravelAgentFilter(ids = NonEmptyList.of(id).some))
    } yield res.headOption match {
      case Some(res) => Right(res)
      case None      => Left(notFound(id))
    }
    program.provideLayer(ZLayer.succeed(tx))
  }
  override def list(filter: ApiListTravelAgentsFilter)(implicit ctx: Ctx): Task[Either[Unit, List[ApiTravelAgent]]] = {
    val program = for {
      _   <- log.info("call list travel agent")
      res <- queryList(ApiConverter.toDomainFilter(filter))
    } yield Right(res)
    program.provideLayer(ZLayer.succeed(tx))
  }
  override def delete(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, Unit]] = {
    log.info(s"call delete travel agent id $id") *>
      agentRepo.delete(id).provideLayer(ZLayer.succeed(tx)) map { deleteCount =>
        if (deleteCount < 1) Left(notFound(id)) else Right(())
      }
  }
  override def update(id: Long, cmd: ApiUpdateTravelAgent)(implicit ctx: Ctx): Task[Either[NotFound, ApiTravelAgent]] = {
    val program = for {
      _           <- log.info(s"call update travel agent category ${cmd}")
      updateCount <- agentRepo.update(ApiConverter.toUpdateTravelAgent(id, cmd))
      res <- if (updateCount > 0) queryList(ListTravelAgentFilter(ids = NonEmptyList.of(id).some))
             else Task.succeed(List.empty)
    } yield res.headOption match {
      case Some(res) => Right(res)
      case None      => Left(notFound(id))
    }
    program.provideLayer(ZLayer.succeed(tx))
  }

  private def queryList(filter: ListTravelAgentFilter)(implicit ctx: Ctx): TranzactIO[List[ApiTravelAgent]] = {
    for {
      dbRows <- agentRepo.list(filter)
      locations <- dbRows.flatMap(_.locations).toNel match {
                     case Some(lIds) => locationRepo.list(ListLocationsFilter(ids = lIds.some))
                     case None       => ZIO(List.empty[Location])
                   }
      hotelCategories <- dbRows.flatMap(_.hotelStarCategories).toNel match {
                           case Some(lIds) => categoriesRepo.list(ListHotelCategoriesFilter(ids = lIds.some))
                           case None       => ZIO(List.empty[HotelStarCategory])
                         }
    } yield {
      val locationMap  = locations.map(e => (e.id, e)).toMap
      val hotelCatsMap = hotelCategories.map(e => (e.id, e)).toMap
      dbRows.map(dbR =>
        ApiConverter.toApiTravelAgent(
          dbR.agent,
          dbR.locations.flatMap(locationMap.get).toList,
          dbR.hotelStarCategories.flatMap(hotelCatsMap.get).toList
        )
      )
    }
  }

  private def notFound(id: Long) = NotFound(s"travel agent id:$id not found")
}

object TravelAgentServiceLive {
  val layer = ZLayer.fromServices[
    TravelAgentRepository,
    LocationRepository,
    HotelStarsRepository,
    Transactor[Task],
    TravelAgentService
  ](new TravelAgentServiceLive(_, _, _, _))
}
