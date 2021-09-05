package ua.kpi.ipsa.service

import doobie.Transactor
import org.slf4j.LoggerFactory
import ua.kpi.ipsa.domain.HotelStarCategory
import ua.kpi.ipsa.dto.filter.ApiListHotelCategoriesFilter
import ua.kpi.ipsa.dto.{ApiCreateHotelStarCategory, ApiHotelStarCategory, ApiUpdateHotelStarCategory}
import ua.kpi.ipsa.repository.HotelStarsRepository
import ua.kpi.ipsa.route.{Conflict, NotFound}
import ua.kpi.ipsa.service.ApiConverter.toApiHotelStarCategory
import ua.kpi.ipsa.trace.{Ctx, log}
import zio._

trait HotelStarsService {
  def create(cmd: ApiCreateHotelStarCategory)(implicit ctx: Ctx): Task[Either[Conflict, ApiHotelStarCategory]]
  def get(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, ApiHotelStarCategory]]
  def list(filter: ApiListHotelCategoriesFilter)(implicit ctx: Ctx): Task[Either[Unit, List[ApiHotelStarCategory]]]
  def delete(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, Unit]]
  def update(id: Long, cmd: ApiUpdateHotelStarCategory)(implicit ctx: Ctx): Task[Either[NotFound, ApiHotelStarCategory]]
}
object HotelStarsService extends Accessible[HotelStarsService]

case class HotelStarsServiceLive(repo: HotelStarsRepository, tx: Transactor[Task]) extends HotelStarsService {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def create(cmd: ApiCreateHotelStarCategory)(implicit ctx: Ctx): Task[Either[Conflict, ApiHotelStarCategory]] = {
    log.info(s"call create hotel stars ${cmd}") *>
      repo
        .create(HotelStarCategory(-1, cmd.stars, cmd.description, cmd.region))
        .flatMap { id => repo.get(id).map(_.get) }
        .provideLayer(ZLayer.succeed(tx)) map { r =>
        Right(toApiHotelStarCategory(r))
      }
  }
  override def get(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, ApiHotelStarCategory]] = {
    log.info(s"call get hotel stars by id $id") *>
      repo.get(id).provideLayer(ZLayer.succeed(tx)) map {
        case Some(r) => Right(toApiHotelStarCategory(r))
        case None    => Left(notFound(id))
      }
  }
  override def list(filter: ApiListHotelCategoriesFilter)(implicit ctx: Ctx): Task[Either[Unit, List[ApiHotelStarCategory]]] = {
    log.info("call list hotel stars") *>
      repo.list(ApiConverter.toDomainFilter(filter)).provideLayer(ZLayer.succeed(tx)) map { list =>
        Right(list.map(r => toApiHotelStarCategory(r)))
      }
  }
  override def delete(id: Long)(implicit ctx: Ctx): Task[Either[NotFound, Unit]] = {
    log.info(s"call delete hotel stars id $id") *>
      repo.delete(id).provideLayer(ZLayer.succeed(tx)) map { deleteCount =>
        if (deleteCount < 1) Left(notFound(id)) else Right(())
      }
  }
  override def update(id: Long, cmd: ApiUpdateHotelStarCategory)(implicit ctx: Ctx): Task[Either[NotFound, ApiHotelStarCategory]] = {
    val program = for {
      _           <- log.info(s"call update hotel stars category ${cmd}")
      updateCount <- repo.update(HotelStarCategory(id, cmd.stars, cmd.description, cmd.region))
      res         <- if (updateCount > 0) repo.get(id) else Task.none
    } yield res
    program.provideLayer(ZLayer.succeed(tx)) map {
      case Some(r) => Right(toApiHotelStarCategory(r))
      case None    => Left(notFound(id))
    }
  }
  private def notFound(id: Long) = NotFound(s"hotel star category id:$id not found")
}

object HotelStarsServiceLive {
  val layer: URLayer[Has[HotelStarsRepository] with Has[Transactor[Task]], Has[HotelStarsService]] =
    (HotelStarsServiceLive(_, _)).toLayer
}
