package ua.kpi.ipsa.service

import doobie.Transactor
import org.slf4j.LoggerFactory
import ua.kpi.ipsa.domain.Location
import ua.kpi.ipsa.domain.filter.ListLocationsFilter
import ua.kpi.ipsa.repository.LocationRepository
import ua.kpi.ipsa.trace.{Ctx, log}
import zio._

trait LocationService {
  def create(cmd: Location)(implicit ctx: Ctx): Task[Location]
  def get(id: Long)(implicit ctx: Ctx): Task[Option[Location]]
  def list(filter: ListLocationsFilter)(implicit ctx: Ctx): Task[List[Location]]
  def delete(id: Long)(implicit ctx: Ctx): Task[Option[Location]]
  def update(cmd: Location)(implicit ctx: Ctx): Task[Option[Location]]
}
object LocationService extends Accessible[LocationService]

case class LocationServiceLive(repo: LocationRepository, tx: Transactor[Task]) extends LocationService {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def create(cmd: Location)(implicit ctx: Ctx): Task[Location] = withTx {
    for {
      _   <- log.info(s"call create location $cmd")
      id  <- repo.create(cmd)
      res <- repo.get(id).map(_.get)
    } yield res
  }
  override def get(id: Long)(implicit ctx: Ctx): Task[Option[Location]] = withTx {
    log.info(s"call get location by id $id") *> repo.get(id)
  }
  override def list(filter: ListLocationsFilter)(implicit ctx: Ctx): Task[List[Location]] = withTx {
    log.info("call list location") *> repo.list(filter)
  }
  override def delete(id: Long)(implicit ctx: Ctx): Task[Option[Location]] = withTx {
    for {
      _   <- log.info(s"call delete location id $id")
      res <- repo.get(id)
      _   <- ZIO.when(res.nonEmpty)(repo.delete(id))
    } yield res
  }
  override def update(cmd: Location)(implicit ctx: Ctx): Task[Option[Location]] = withTx {
    for {
      _           <- log.info(s"call update location category ${cmd}")
      updateCount <- repo.update(cmd)
      res         <- if (updateCount > 0) repo.get(cmd.id) else Task.none
    } yield res
  }
  private def withTx[R, E, A](program: ZIO[Has[Transactor[Task]], E, A]): ZIO[R, E, A] =
    program.provideLayer(ZLayer.succeed(tx))
}

object LocationServiceLive {
  val layer = ZLayer.fromServices[
    LocationRepository,
    Transactor[Task],
    LocationService
  ](new LocationServiceLive(_, _))
}
