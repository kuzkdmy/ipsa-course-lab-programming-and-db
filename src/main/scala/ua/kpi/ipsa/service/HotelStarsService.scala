package ua.kpi.ipsa.service

import doobie.Transactor
import org.slf4j.LoggerFactory
import ua.kpi.ipsa.domain.HotelStarCategory
import ua.kpi.ipsa.domain.filter.ListHotelCategoriesFilter
import ua.kpi.ipsa.repository.HotelStarsRepository
import ua.kpi.ipsa.trace.{Ctx, log}
import zio._

trait HotelStarsService {
  def create(cmd: HotelStarCategory)(implicit ctx: Ctx): Task[HotelStarCategory]
  def get(id: Long)(implicit ctx: Ctx): Task[Option[HotelStarCategory]]
  def list(filter: ListHotelCategoriesFilter)(implicit ctx: Ctx): Task[List[HotelStarCategory]]
  def delete(id: Long)(implicit ctx: Ctx): Task[Option[HotelStarCategory]]
  def update(cmd: HotelStarCategory)(implicit ctx: Ctx): Task[Option[HotelStarCategory]]
}
object HotelStarsService extends Accessible[HotelStarsService]

case class HotelStarsServiceLive(repo: HotelStarsRepository, tx: Transactor[Task]) extends HotelStarsService {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def create(cmd: HotelStarCategory)(implicit ctx: Ctx): Task[HotelStarCategory] = withTx {
    for {
      _   <- log.info(s"call create hotel stars ${cmd}")
      id  <- repo.create(cmd)
      res <- repo.get(id).map(_.get)
    } yield res
  }
  override def get(id: Long)(implicit ctx: Ctx): Task[Option[HotelStarCategory]] = withTx {
    log.info(s"call get hotel stars by id $id") *> repo.get(id)
  }
  override def list(filter: ListHotelCategoriesFilter)(implicit ctx: Ctx): Task[List[HotelStarCategory]] = withTx {
    log.info("call list hotel stars") *> repo.list(filter)
  }
  override def delete(id: Long)(implicit ctx: Ctx): Task[Option[HotelStarCategory]] = withTx {
    for {
      _   <- log.info(s"call delete hotel stars id $id")
      res <- repo.get(id)
      _   <- ZIO.when(res.nonEmpty)(repo.delete(id))
    } yield res
  }
  override def update(cmd: HotelStarCategory)(implicit ctx: Ctx): Task[Option[HotelStarCategory]] = withTx {
    for {
      _           <- log.info(s"call update hotel stars category ${cmd}")
      updateCount <- repo.update(cmd)
      res         <- if (updateCount > 0) repo.get(cmd.id) else Task.none
    } yield res
  }
  private def withTx[R, E, A](program: ZIO[Has[Transactor[Task]], E, A]): ZIO[R, E, A] =
    program.provideLayer(ZLayer.succeed(tx))
}

object HotelStarsServiceLive {
  val layer = ZLayer.fromServices[
    HotelStarsRepository,
    Transactor[Task],
    HotelStarsService
  ](HotelStarsServiceLive(_, _))
}
