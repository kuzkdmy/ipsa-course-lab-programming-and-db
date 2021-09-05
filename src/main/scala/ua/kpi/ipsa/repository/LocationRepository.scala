package ua.kpi.ipsa.repository

import cats.data.NonEmptyList
import cats.syntax.list._
import cats.syntax.option._
import doobie.implicits._
import doobie.util.fragments.{in, whereAndOpt}
import ua.kpi.ipsa.domain.Location
import ua.kpi.ipsa.domain.filter.ListLocationsFilter
import ua.kpi.ipsa.trace.Ctx
import zio.{Has, ULayer, ZLayer}

trait LocationRepository {
  def create(cmd: Location)(implicit ctx: Ctx): TranzactIO[Long]
  def get(id: Long)(implicit ctx: Ctx): TranzactIO[Option[Location]]
  def list(filter: ListLocationsFilter)(implicit ctx: Ctx): TranzactIO[List[Location]]
  def delete(id: Long)(implicit ctx: Ctx): TranzactIO[Int]
  def update(cmd: Location)(implicit ctx: Ctx): TranzactIO[Int]
}

case class LocationRepositoryLive() extends LocationRepository {
  override def create(cmd: Location)(implicit ctx: Ctx): TranzactIO[Long] = tzio {
    sql"INSERT INTO locations(name, location_type, parent_id) VALUES (${cmd.name}, ${cmd.locationType}, ${cmd.parentLocationId})".update
      .withUniqueGeneratedKeys[Long]("id")
  }

  override def get(id: Long)(implicit ctx: Ctx): TranzactIO[Option[Location]] =
    list(ListLocationsFilter(ids = NonEmptyList.of(id).some)).map(_.headOption)

  override def list(filter: ListLocationsFilter)(implicit ctx: Ctx): TranzactIO[List[Location]] = tzio {
    val filterFragment = whereAndOpt(
      filter.ids.map(ids => in(fr"id", ids))
    )
    for {
      ids <- (sql"SELECT distinct id FROM locations " ++ filterFragment ++ fr"ORDER BY id " ++ filter.limit.map(l => fr"LIMIT $l").getOrElse(fr"")).query[Long].to[Set]
      res <- (sql"SELECT id, name, location_type, parent_id FROM locations " ++ whereAndOpt(ids.toList.toNel.map(ids => in(fr"id", ids)))).query[Location].to[List]
    } yield res
  }

  override def delete(id: Long)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"DELETE FROM locations WHERE id = $id".update.run
  }

  override def update(cmd: Location)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    sql"UPDATE locations SET name = ${cmd.name}, location_type = ${cmd.locationType}, parent_id = ${cmd.parentLocationId}  where id = ${cmd.id}".update.run
  }
}

object LocationRepositoryLive {
  val layer: ULayer[Has[LocationRepository]] = ZLayer.succeed(LocationRepositoryLive())
}
