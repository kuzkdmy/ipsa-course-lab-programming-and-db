package ua.kpi.ipsa.repository

import cats.data.NonEmptyList
import cats.implicits.catsSyntaxOptionId
import cats.syntax.list._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.fragments.{in, whereAnd, whereAndOpt}
import ua.kpi.ipsa.domain.TravelVoucher
import ua.kpi.ipsa.domain.filter.ListTravelVouchersFilter
import ua.kpi.ipsa.trace.Ctx
import zio.{Has, ULayer, ZLayer}

trait TravelVoucherRepository {
  def create(cmd: TravelVoucher)(implicit ctx: Ctx): TranzactIO[Long]
  def get(id: Long)(implicit ctx: Ctx): TranzactIO[Option[TravelVoucher]]
  def list(filter: ListTravelVouchersFilter)(implicit ctx: Ctx): TranzactIO[List[TravelVoucher]]
  def delete(id: Long)(implicit ctx: Ctx): TranzactIO[Int]
  def update(cmd: TravelVoucher)(implicit ctx: Ctx): TranzactIO[Int]
}

case class TravelVoucherRepositoryLive() extends TravelVoucherRepository {
  override def create(cmd: TravelVoucher)(implicit ctx: Ctx): TranzactIO[Long] = tzio {
    sql"""INSERT INTO travel_voucher
        (travel_agent_id     , created_at      , updated_at      , voucher_start_date     , voucher_end_date     , users) VALUES
        (${cmd.travelAgentId}, ${cmd.createdAt}, ${cmd.updatedAt}, ${cmd.voucherStartDate}, ${cmd.voucherEndDate}, ${cmd.users})""".update
      .withUniqueGeneratedKeys[Long]("id")
  }

  override def get(id: Long)(implicit ctx: Ctx): TranzactIO[Option[TravelVoucher]] =
    list(ListTravelVouchersFilter(ids = NonEmptyList.of(id).some)).map(_.headOption)

  override def list(filter: ListTravelVouchersFilter)(implicit ctx: Ctx): TranzactIO[List[TravelVoucher]] = tzio {
    val filterFragment = whereAndOpt(
      filter.ids.map(e => in(fr"tv.id", e)),
      filter.agents.map(e => in(fr"ta.id", e)),
      filter.createdAt.map(e => in(fr"date(tv.created_at)", e)),
      filter.createdAtGE.map(e => fr"date(tv.created_at)>=$e"),
      filter.createdAtLT.map(e => fr"date(tv.created_at)<$e"),
      filter.updatedAt.map(e => in(fr"date(tv.updated_at)", e)),
      filter.updatedAtGE.map(e => fr"date(tv.updated_at)>=$e"),
      filter.updatedAtLT.map(e => fr"date(tv.updated_at)<$e"),
      filter.startAt.map(e => in(fr"date(tv.voucher_start_date)", e)),
      filter.startAtGE.map(e => fr"date(tv.voucher_start_date)>=$e"),
      filter.startAtLT.map(e => fr"date(tv.voucher_start_date)<$e"),
      filter.endAt.map(e => in(fr"date(tv.voucher_end_date)", e)),
      filter.endAtGE.map(e => fr"date(tv.voucher_end_date)>=$e"),
      filter.endAtLT.map(e => fr"date(tv.voucher_end_date)<$e"),
      filter.lastNames.map(_.map(_.toLowerCase)).map { e =>
        in(fr"exists(select tv2.id from (select id, unnest(users) as u from travel_voucher where id = tv.id) tv2 where lower(tv2.u ->> 'lastName')", e) ++ fr")"
      },
      filter.passports.map(_.map(_.toUpperCase)).map { e =>
        in(fr"exists(select tv2.id from (select id, unnest(users) as u from travel_voucher where id = tv.id) tv2 where upper(tv2.u ->> 'passportNumber')", e) ++ fr")"
      }
    )
    val limitFragment = filter.queryLimit.map(l => fr" LIMIT $l").getOrElse(fr"")
    for {
      ids <- (sql"""SELECT distinct tv.id FROM travel_voucher tv
                JOIN travel_agent ta on tv.travel_agent_id = ta.id
             """ ++ filterFragment ++ fr" ORDER BY tv.id " ++ limitFragment).query[Long].to[Set]
      res <- (sql"SELECT id, travel_agent_id, created_at, updated_at, voucher_start_date, voucher_end_date, users FROM travel_voucher " ++ whereAnd(
               ids.toList.toNel.fold(fr" false")(ids => in(fr" id", ids))
             ))
               .query[TravelVoucher]
               .to[List]
    } yield res
  }

  override def delete(id: Long)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"DELETE FROM travel_voucher WHERE id = $id".update.run
  }

  override def update(cmd: TravelVoucher)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"""UPDATE travel_voucher SET 
                          travel_agent_id = ${cmd.travelAgentId}, 
                          updated_at = ${cmd.updatedAt},
                          voucher_start_date = ${cmd.voucherStartDate},
                          voucher_end_date = ${cmd.voucherEndDate},
                          users = ${cmd.users}
            WHERE id = ${cmd.id}""".update.run
  }
}

object TravelVoucherRepositoryLive {
  val layer: ULayer[Has[TravelVoucherRepository]] = ZLayer.succeed(TravelVoucherRepositoryLive())
}
