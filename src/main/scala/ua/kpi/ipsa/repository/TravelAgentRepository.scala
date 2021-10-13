package ua.kpi.ipsa.repository

import cats.data.NonEmptyList
import cats.implicits.toFoldableOps
import cats.syntax.list._
import cats.syntax.option._
import doobie.Update
import doobie.free.connection
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.fragment.Fragment.const
import doobie.util.fragments.{and, in, whereAnd, whereAndOpt}
import ua.kpi.ipsa.domain.filter.ListTravelAgentFilter
import ua.kpi.ipsa.domain.{TravelAgentQueryListRow, TravelAgentRepr, TravelAgentRow}
import ua.kpi.ipsa.trace.Ctx
import zio.{Has, ULayer, ZLayer}

trait TravelAgentRepository {
  def create(cmd: TravelAgentRepr)(implicit ctx: Ctx): TranzactIO[Long]
  def update(cmd: TravelAgentRepr)(implicit ctx: Ctx): TranzactIO[Int]
  def delete(id: Long)(implicit ctx: Ctx): TranzactIO[Int]
  def get(id: Long)(implicit ctx: Ctx): TranzactIO[Option[TravelAgentRepr]]
  def list(filter: ListTravelAgentFilter)(implicit ctx: Ctx): TranzactIO[List[TravelAgentRepr]]
}

case class TravelAgentRepositoryLive() extends TravelAgentRepository {
  override def create(cmd: TravelAgentRepr)(implicit ctx: Ctx): TranzactIO[Long] = tzio {
    for {
      id <- sql"INSERT INTO travel_agent(name, photos) VALUES (${cmd.agent.name}, ${cmd.agent.photos})".update.withUniqueGeneratedKeys[Long]("id")
      _  <- insertCategories(id, cmd.hotelStarCategories)
      _  <- insertLocations(id, cmd.locations)
    } yield id
  }

  override def get(id: Long)(implicit ctx: Ctx): TranzactIO[Option[TravelAgentRepr]] =
    list(ListTravelAgentFilter(ids = NonEmptyList.of(id).some)).map(_.headOption)

  override def list(filter: ListTravelAgentFilter)(implicit ctx: Ctx): TranzactIO[List[TravelAgentRepr]] = {
    val filterFragment = whereAndOpt(
      filter.ids.map(e => in(fr"t.id", e)),
      filter.namesIn.map(e => in(fr"lower(t.name)", e.map(_.toLowerCase))),
      filter.countriesIn.map(e => and(fr"l.location_type='country'", in(fr"lower(l.name)", e.map(_.toLowerCase)))),
      filter.citiesIn.map(e => and(fr"l.location_type='city'", in(fr"lower(l.name)", e.map(_.toLowerCase)))),
      filter.photosIn.map(e => fr"lower(t.photos::text)::text[] && " ++ fr0"'{" ++ const(e.toList.map(_.toLowerCase).intercalate(",")) ++ fr"}'"),
      filter.hotelStarsIn.map(e => in(fr"s.stars", e))
    )
    tzio {
      for {
        ids <- (sql"""SELECT distinct t.id FROM travel_agent t
                      LEFT JOIN travel_agent_categories tc ON t.id = tc.travel_agent_id
                      LEFT JOIN travel_agent_locations tl ON t.id = tl.travel_agent_id
                      LEFT JOIN locations l ON l.id = tl.location_id
                      LEFT JOIN hotel_starts_categories s ON s.id = tc.hotel_star_category_id
                      """ ++
                 filterFragment ++ fr" ORDER BY t.id " ++ filter.queryLimit.map(l => fr"LIMIT $l").getOrElse(fr""))
                 .query[Long]
                 .to[Set]
        dataTups <- (sql"""SELECT t.id, t.name, t.photos, tc.hotel_star_category_id, tl.location_id FROM travel_agent t
                      LEFT JOIN travel_agent_categories tc ON t.id = tc.travel_agent_id
                      LEFT JOIN travel_agent_locations tl ON t.id = tl.travel_agent_id """ ++
                      whereAnd(ids.toList.toNel.fold(fr" false")(ids => in(fr"t.id", ids)))++ fr" ORDER BY t.id ").query[TravelAgentQueryListRow].to[List]
      } yield {
        dataTups
          .groupBy(_.id)
          .toList
          .collect { case (_, byId) =>
            byId.toNel match {
              case Some(nel) =>
                List(
                  TravelAgentRepr(
                    agent = TravelAgentRow(
                      id     = nel.head.id,
                      name   = nel.head.name,
                      photos = nel.head.photos
                    ),
                    locations           = nel.toList.flatMap(_.locationId).toSet,
                    hotelStarCategories = nel.toList.flatMap(_.hotelCategoryId).toSet
                  )
                )
              case None => Nil
            }
          }
          .flatten
          .sortBy(_.agent.id)
      }
    }
  }

  override def delete(id: Long)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    for {
      deleteCount <- sql"DELETE FROM travel_agent WHERE id = $id".update.run
      _           <- deleteCategories(id)
      _           <- deleteLocations(id)
    } yield deleteCount
  }

  override def update(cmd: TravelAgentRepr)(implicit ctx: Ctx): TranzactIO[Int] = tzio {
    for {
      updateCount <- sql"""UPDATE travel_agent SET name = ${cmd.agent.name}, photos = ${cmd.agent.photos} WHERE id = ${cmd.agent.id}""".update.run
      _           <- deleteCategories(cmd.agent.id)
      _           <- deleteLocations(cmd.agent.id)
      _           <- insertCategories(cmd.agent.id, cmd.hotelStarCategories)
      _           <- insertLocations(cmd.agent.id, cmd.locations)
    } yield updateCount
  }

  private def deleteCategories(agentId: Long) =
    sql"DELETE FROM travel_agent_categories WHERE travel_agent_id = $agentId".update.run
  private def deleteLocations(agentId: Long) =
    sql"DELETE FROM travel_agent_locations WHERE travel_agent_id = $agentId".update.run
  private def insertCategories(agentId: Long, categories: Set[Long]) =
    NonEmptyList.fromList(categories.toList) match {
      case Some(categories) =>
        val sql = "INSERT INTO travel_agent_categories(travel_agent_id, hotel_star_category_id) VALUES (?,?)"
        Update[(Long, Long)](sql).updateMany(categories.map(cId => (agentId, cId)))
      case None =>
        connection.unit
    }
  private def insertLocations(agentId: Long, locations: Set[Long]) =
    NonEmptyList.fromList(locations.toList) match {
      case Some(locations) =>
        val sql = "INSERT INTO travel_agent_locations(travel_agent_id, location_id) VALUES (?,?)"
        Update[(Long, Long)](sql).updateMany(locations.map(lId => (agentId, lId)))
      case None =>
        connection.unit
    }
}

object TravelAgentRepositoryLive {
  val layer: ULayer[Has[TravelAgentRepository]] = ZLayer.succeed(TravelAgentRepositoryLive())
}
