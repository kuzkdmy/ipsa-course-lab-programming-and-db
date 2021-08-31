package ua.kpi.ipsa.service

import doobie.Transactor
import org.slf4j.LoggerFactory
import ua.kpi.ipsa.domain.HotelStarCategory
import ua.kpi.ipsa.repository.HotelStarsRepository
import ua.kpi.ipsa.trace.{log, Ctx}
import zio._

trait HotelStarsService {
  def create(cmd: HotelStarCategory)(implicit ctx: Ctx): Task[HotelStarCategory]
  def get(id: Int)(implicit ctx: Ctx): Task[Option[HotelStarCategory]]
  def list()(implicit ctx: Ctx): Task[List[HotelStarCategory]]
  def delete(id: Int)(implicit ctx: Ctx): Task[Int]
  def update(cmd: HotelStarCategory)(implicit ctx: Ctx): Task[Option[HotelStarCategory]]
}
object HotelStarsService extends Accessible[HotelStarsService]

case class HotelStarsServiceLive(repo: HotelStarsRepository, tx: Transactor[Task]) extends HotelStarsService {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def create(cmd: HotelStarCategory)(implicit ctx: Ctx): Task[HotelStarCategory] = {
    log.info(s"call create hotel starts ${cmd}") *>
      repo.create(cmd).flatMap(id => repo.get(id).map(_.get)).provideLayer(ZLayer.succeed(tx))
  }
  override def get(id: Int)(implicit ctx: Ctx): Task[Option[HotelStarCategory]] = {
    log.info(s"call get hotel starts by id $id") *>
      repo.get(id).provideLayer(ZLayer.succeed(tx))
  }
  override def list()(implicit ctx: Ctx): Task[List[HotelStarCategory]] = {
    log.info("call list hotel starts") *>
      repo.list().provideLayer(ZLayer.succeed(tx))
  }
  override def delete(id: Int)(implicit ctx: Ctx): Task[Int] = {
    log.info(s"call delete hotel starts id $id") *>
      repo.delete(id).provideLayer(ZLayer.succeed(tx))
  }
  override def update(cmd: HotelStarCategory)(implicit ctx: Ctx): Task[Option[HotelStarCategory]] = {
    val program = for {
      _           <- log.info(s"call update hotel starts category ${cmd}")
      updateCount <- repo.update(cmd)
      res         <- if (updateCount > 0) repo.get(cmd.id) else Task.none
    } yield res
    program.provideLayer(ZLayer.succeed(tx))
  }
}

object HotelStarsServiceLive {
  val layer: URLayer[Has[HotelStarsRepository] with Has[Transactor[Task]], Has[HotelStarsService]] =
    (HotelStarsServiceLive(_, _)).toLayer
}
