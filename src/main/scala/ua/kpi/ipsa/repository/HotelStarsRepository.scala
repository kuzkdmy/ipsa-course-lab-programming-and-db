package ua.kpi.ipsa.repository

import doobie.implicits._
import ua.kpi.ipsa.domain.HotelStarCategory
import ua.kpi.ipsa.trace.Ctx
import zio.{Has, ULayer, ZLayer}

trait HotelStarsRepository {
  def create(cmd: HotelStarCategory)(implicit ctx: Ctx): TranzactIO[Int]
  def get(id: Int)(implicit ctx: Ctx): TranzactIO[Option[HotelStarCategory]]
  def list()(implicit ctx: Ctx): TranzactIO[List[HotelStarCategory]]
  def delete(id: Int)(implicit ctx: Ctx): TranzactIO[Int]
  def update(cmd: HotelStarCategory)(implicit ctx: Ctx): TranzactIO[Int]
}

case class HotelStarsRepositoryLive() extends HotelStarsRepository {
  override def create(cmd: HotelStarCategory)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"INSERT INTO ref_hotel_stars_categories(stars, description, region) VALUES (${cmd.stars}, ${cmd.description}, ${cmd.region})".update
      .withUniqueGeneratedKeys[Int]("id")
  }

  override def get(id: Int)(implicit ctx: Ctx): TranzactIO[Option[HotelStarCategory]] = tzio {
    sql"SELECT id, stars, description, region FROM ref_hotel_stars_categories".query[HotelStarCategory].option
  }

  override def list()(implicit ctx: Ctx): TranzactIO[List[HotelStarCategory]] = {
    tzio {
      sql"SELECT id, stars, description, region FROM ref_hotel_stars_categories"
        .query[HotelStarCategory]
        .to[List]
    }
  }

  override def delete(id: Int)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"DELETE FROM ref_hotel_stars_categories WHERE id = $id".update.run
  }

  override def update(cmd: HotelStarCategory)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"UPDATE ref_hotel_stars_categories SET stars = ${cmd.stars}, description = ${cmd.description}, region = ${cmd.region}  where id = ${cmd.id}".update.run
  }
}

object HotelStarsRepositoryLive {
  val layer: ULayer[Has[HotelStarsRepository]] = ZLayer.succeed(HotelStarsRepositoryLive())
}
