package ua.kpi.ipsa.service

import doobie.Transactor
import org.slf4j.LoggerFactory
import ua.kpi.ipsa.domain.Location
import ua.kpi.ipsa.dto.ApiLocation
import ua.kpi.ipsa.dto.filter.ApiListLocationsFilter
import ua.kpi.ipsa.repository.LocationRepository
import ua.kpi.ipsa.route.{Conflict, NotFound}
import ua.kpi.ipsa.service.ApiConverter.toApiLocation
import ua.kpi.ipsa.trace.{Ctx, log}
import zio._

trait LocationService {
  def create(cmd: Location)(implicit ctx: Ctx): Task[Either[Conflict, ApiLocation]]
  def get(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, ApiLocation]]
  def list(filter: ApiListLocationsFilter)(implicit ctx: Ctx): Task[Either[Unit, List[ApiLocation]]]
  def delete(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, Unit]]
  def update(cmd: Location)(implicit ctx: Ctx): Task[Either[NotFound, ApiLocation]]
}
object LocationService extends Accessible[LocationService]

case class LocationServiceLive(repo: LocationRepository, tx: Transactor[Task]) extends LocationService {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def create(cmd: Location)(implicit ctx: Ctx): Task[Either[Conflict, ApiLocation]] = {
    log.info(s"call create location ${cmd}") *>
      repo.create(cmd).flatMap(id => repo.get(id).map(_.get)).provideLayer(ZLayer.succeed(tx)) map { r =>
        Right(toApiLocation(r))
      }
  }
  override def get(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, ApiLocation]] = {
    log.info(s"call get location by id $id") *>
      repo.get(id).provideLayer(ZLayer.succeed(tx)) map {
        case Some(r) => Right(toApiLocation(r))
        case None    => Left(notFound(id))
      }
  }
  override def list(filter: ApiListLocationsFilter)(implicit ctx: Ctx): Task[Either[Unit, List[ApiLocation]]] = {
    log.info("call list location") *>
      repo.list(ApiConverter.toDomainFilter(filter)).provideLayer(ZLayer.succeed(tx)) map { list =>
        Right(list.map(r => toApiLocation(r)))
      }
  }
  override def delete(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, Unit]] = {
    log.info(s"call delete location id $id") *>
      repo.delete(id).provideLayer(ZLayer.succeed(tx)).map { deleteCount =>
        if (deleteCount < 1) Left(notFound(id)) else Right(())
      }
  }
  override def update(cmd: Location)(implicit ctx: Ctx): Task[Either[NotFound, ApiLocation]] = {
    val program = for {
      _           <- log.info(s"call update location category ${cmd}")
      updateCount <- repo.update(cmd)
      res         <- if (updateCount > 0) repo.get(cmd.id) else Task.none
    } yield res
    program.provideLayer(ZLayer.succeed(tx)) map {
      case Some(r) => Right(toApiLocation(r))
      case None    => Left(notFound(cmd.id))
    }
  }
  private def notFound(id: Long) = NotFound(s"location id:$id not found")
}

object LocationServiceLive {
  val layer: URLayer[Has[LocationRepository] with Has[Transactor[Task]], Has[LocationService]] =
    (LocationServiceLive(_, _)).toLayer
}
