package ua.kpi.ipsa.service

import cats.data.NonEmptyList
import cats.implicits.toShow
import cats.syntax.list._
import cats.syntax.option._
import doobie.Transactor
import org.slf4j.LoggerFactory
import ua.kpi.ipsa.domain.filter.{ListHotelCategoriesFilter, ListLocationsFilter, ListTravelAgentFilter}
import ua.kpi.ipsa.domain.{HotelStarCategory, Location, TravelAgent, TravelAgentRepr}
import ua.kpi.ipsa.repository.{HotelStarsRepository, LocationRepository, TranzactIO, TravelAgentRepository}
import ua.kpi.ipsa.trace.{Ctx, log}
import zio._

trait TravelAgentService {
  def create(cmd: TravelAgentRepr)(implicit ctx: Ctx): Task[TravelAgent]
  def get(id: Long)(implicit ctx: Ctx): Task[Option[TravelAgent]]
  def list(filter: ListTravelAgentFilter)(implicit ctx: Ctx): Task[List[TravelAgent]]
  def delete(id: Long)(implicit ctx: Ctx): Task[Option[TravelAgent]]
  def update(cmd: TravelAgentRepr)(implicit ctx: Ctx): Task[Option[TravelAgent]]
}
object TravelAgentService extends Accessible[TravelAgentService]

case class TravelAgentServiceLive(
    agentRepo: TravelAgentRepository,
    locationRepo: LocationRepository,
    categoriesRepo: HotelStarsRepository,
    tx: Transactor[Task]
) extends TravelAgentService {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def create(cmd: TravelAgentRepr)(implicit ctx: Ctx): Task[TravelAgent] = withTx {
    for {
      _   <- log.info(s"call create travel agent $cmd")
      id  <- agentRepo.create(cmd)
      res <- queryList(ListTravelAgentFilter(ids = NonEmptyList.of(id).some))
    } yield res.headOption.get
  }
  override def get(id: Long)(implicit ctx: Ctx): Task[Option[TravelAgent]] = withTx {
    for {
      _   <- log.info(s"call get travel agent by id $id")
      res <- queryList(ListTravelAgentFilter(ids = NonEmptyList.of(id).some))
    } yield res.headOption
  }
  override def list(filter: ListTravelAgentFilter)(implicit ctx: Ctx): Task[List[TravelAgent]] = withTx {
    for {
      _   <- log.info(s"call list travel agent ${filter.show}")
      res <- queryList(filter)
    } yield res
  }
  override def delete(id: Long)(implicit ctx: Ctx): Task[Option[TravelAgent]] = withTx {
    for {
      _   <- log.info(s"call delete travel agent id $id")
      res <- queryList(ListTravelAgentFilter(ids = NonEmptyList.of(id).some)).map(_.headOption)
      _   <- ZIO.when(res.nonEmpty)(agentRepo.delete(id))
    } yield res
  }
  override def update(cmd: TravelAgentRepr)(implicit ctx: Ctx): Task[Option[TravelAgent]] = withTx {
    for {
      _           <- log.info(s"call update travel agent category ${cmd}")
      updateCount <- agentRepo.update(cmd)
      res <- if (updateCount > 0) queryList(ListTravelAgentFilter(ids = NonEmptyList.of(cmd.agent.id).some))
             else Task.succeed(List.empty)
    } yield res.headOption
  }

  private def queryList(filter: ListTravelAgentFilter)(implicit ctx: Ctx): TranzactIO[List[TravelAgent]] = {
    for {
      dbRows <- agentRepo.list(filter)
      locations <- dbRows.flatMap(_.locations).distinct.toNel match {
                     case Some(lIds) => locationRepo.list(ListLocationsFilter(ids = lIds.some))
                     case None       => ZIO(List.empty[Location])
                   }
      hotelCategories <- dbRows.flatMap(_.hotelStarCategories).distinct.toNel match {
                           case Some(lIds) => categoriesRepo.list(ListHotelCategoriesFilter(ids = lIds.some))
                           case None       => ZIO(List.empty[HotelStarCategory])
                         }
    } yield {
      val locationMap  = locations.map(e => (e.id, e)).toMap
      val hotelCatsMap = hotelCategories.map(e => (e.id, e)).toMap
      dbRows.map(dbR =>
        TravelAgent(
          id                = dbR.agent.id,
          name              = dbR.agent.name,
          locations         = dbR.locations.flatMap(locationMap.get).toList,
          photos            = dbR.agent.photos,
          hotelStarCategory = dbR.hotelStarCategories.flatMap(hotelCatsMap.get).toList
        )
      )
    }
  }
  private def withTx[R, E, A](program: ZIO[Has[Transactor[Task]], E, A]): ZIO[R, E, A] =
    program.provideLayer(ZLayer.succeed(tx))
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
