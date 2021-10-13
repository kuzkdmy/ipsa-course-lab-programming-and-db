package ua.kpi.ipsa.service

import doobie.Transactor
import org.slf4j.LoggerFactory
import ua.kpi.ipsa.domain.TravelVoucher
import ua.kpi.ipsa.domain.filter.ListTravelVouchersFilter
import ua.kpi.ipsa.repository.TravelVoucherRepository
import ua.kpi.ipsa.trace._
import zio._

trait TravelVoucherService {
  def create(cmd: TravelVoucher)(implicit ctx: Ctx): Task[TravelVoucher]
  def get(id: Long)(implicit ctx: Ctx): Task[Option[TravelVoucher]]
  def list(filter: ListTravelVouchersFilter)(implicit ctx: Ctx): Task[List[TravelVoucher]]
  def delete(id: Long)(implicit ctx: Ctx): Task[Option[TravelVoucher]]
  def update(cmd: TravelVoucher)(implicit ctx: Ctx): Task[Option[TravelVoucher]]
}
object TravelVoucherService extends Accessible[TravelVoucherService]

case class TravelVoucherServiceLive(repo: TravelVoucherRepository, tx: Transactor[Task]) extends TravelVoucherService {
  implicit private val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  override def create(cmd: TravelVoucher)(implicit ctx: Ctx): Task[TravelVoucher] = withTx {
    for {
      _   <- log.info(s"call create travel voucher ${cmd}")
      id  <- repo.create(cmd)
      res <- repo.get(id).map(_.get)
    } yield res
  }
  override def get(id: Long)(implicit ctx: Ctx): Task[Option[TravelVoucher]] = withTx {
    log.info(s"call get travel voucher by id $id") *> repo.get(id)
  }
  override def list(filter: ListTravelVouchersFilter)(implicit ctx: Ctx): Task[List[TravelVoucher]] = withTx {
    log.info("call list travel voucher") *> repo.list(filter)
  }
  override def delete(id: Long)(implicit ctx: Ctx): Task[Option[TravelVoucher]] = withTx {
    for {
      _   <- log.info(s"call delete travel voucher id $id")
      res <- repo.get(id)
      _   <- ZIO.when(res.nonEmpty)(repo.delete(id))
    } yield res
  }
  override def update(cmd: TravelVoucher)(implicit ctx: Ctx): Task[Option[TravelVoucher]] = withTx {
    for {
      _           <- log.info(s"call update travel voucher category ${cmd}")
      updateCount <- repo.update(cmd)
      res         <- if (updateCount > 0) repo.get(cmd.id) else Task.none
    } yield res
  }
  private def withTx[R, E, A](program: ZIO[Has[Transactor[Task]], E, A]): ZIO[R, E, A] =
    program.provideLayer(ZLayer.succeed(tx))
}

object TravelVoucherServiceLive {
  val layer = ZLayer.fromServices[
    TravelVoucherRepository,
    Transactor[Task],
    TravelVoucherService
  ](TravelVoucherServiceLive(_, _))
}
