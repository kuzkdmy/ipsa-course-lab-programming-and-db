package ua.kpi.ipsa.repository

import cats.data.NonEmptyList
import cats.implicits.catsSyntaxOptionId
import cats.syntax.list._
import doobie.implicits._
import doobie.util.fragments.{in, whereAnd, whereAndOpt}
import ua.kpi.ipsa.domain.HotelStarCategory
import ua.kpi.ipsa.domain.filter.ListHotelCategoriesFilter
import ua.kpi.ipsa.trace.Ctx
import zio.{Has, ULayer, ZLayer}

trait HotelStarsRepository {
  def create(cmd: HotelStarCategory)(implicit ctx: Ctx): TranzactIO[Long]
  def get(id: Long)(implicit ctx: Ctx): TranzactIO[Option[HotelStarCategory]]
  def list(filter: ListHotelCategoriesFilter)(implicit ctx: Ctx): TranzactIO[List[HotelStarCategory]]
  def delete(id: Long)(implicit ctx: Ctx): TranzactIO[Int]
  def update(cmd: HotelStarCategory)(implicit ctx: Ctx): TranzactIO[Int]
}

case class HotelStarsRepositoryLive() extends HotelStarsRepository {
  override def create(cmd: HotelStarCategory)(implicit ctx: Ctx): TranzactIO[Long] = tzio {
    sql"INSERT INTO hotel_starts_categories(stars, description, region) VALUES (${cmd.stars}, ${cmd.description}, ${cmd.region})".update
      .withUniqueGeneratedKeys[Long]("id")
  }

  override def get(id: Long)(implicit ctx: Ctx): TranzactIO[Option[HotelStarCategory]] =
    list(ListHotelCategoriesFilter(ids = NonEmptyList.of(id).some)).map(_.headOption)

  override def list(filter: ListHotelCategoriesFilter)(implicit ctx: Ctx): TranzactIO[List[HotelStarCategory]] = tzio {
    val filterFragment = whereAndOpt(
      filter.ids.map(ids => in(fr" id", ids))
    )
    for {
      ids <- (sql"SELECT distinct id FROM hotel_starts_categories " ++ filterFragment ++ fr" ORDER BY id " ++ filter.limit.map(l => fr" LIMIT $l").getOrElse(fr"")).query[Long].to[Set]
      res <- (sql"SELECT id, stars, description, region FROM hotel_starts_categories " ++ whereAnd(ids.toList.toNel.fold(fr" false")(ids => in(fr" id", ids)))).query[HotelStarCategory].to[List]
    } yield res
  }

  override def delete(id: Long)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"DELETE FROM hotel_starts_categories WHERE id = $id".update.run
  }

  override def update(cmd: HotelStarCategory)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"UPDATE hotel_starts_categories SET stars = ${cmd.stars}, description = ${cmd.description}, region = ${cmd.region}  where id = ${cmd.id}".update.run
  }
}

object HotelStarsRepositoryLive {
  val layer: ULayer[Has[HotelStarsRepository]] = ZLayer.succeed(HotelStarsRepositoryLive())
}
