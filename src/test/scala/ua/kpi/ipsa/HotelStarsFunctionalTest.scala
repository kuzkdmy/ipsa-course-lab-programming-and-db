package ua.kpi.ipsa

import sttp.client3.Response
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.quick._
import ua.kpi.ipsa.MainApp.appLayer
import ua.kpi.ipsa.domain.types.{HotelStarDescription, HotelStarRegion, HotelStars}
import ua.kpi.ipsa.dto.{ApiCreateHotelStarCategory, ApiHotelStarCategory, ApiUpdateHotelStarCategory}
import zio._
import zio.json._
import zio.test.Assertion.equalTo
import zio.test.assert

object HotelStarsFunctionalTest extends BaseFunTest {
  override def spec = suite("Hotel Stars Management")(
    testM("check create") {
      (for {
        _ <- evalDb("clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          created <- c.post(baseUri).body(ApiCreateHotelStarCategory(stars5, HotelStarDescription("5 star Egypt"), egyptRegion).toJson).send(b).flatMap(asHotel)
        } yield {
          assert(created.stars)(equalTo(stars5)) &&
          assert(created.description)(equalTo(HotelStarDescription("5 star Egypt"))) &&
          assert(created.region)(equalTo(egyptRegion))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check get single") {
      (for {
        _ <- evalDb("clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          notFound <- c.get(baseUri.addPath("123")).send(b)
          created  <- c.post(baseUri).body(ApiCreateHotelStarCategory(stars5, HotelStarDescription("5 star Egypt"), egyptRegion).toJson).send(b).flatMap(asHotel)
          loaded   <- c.get(baseUri.addPath(created.id.toString)).send(b).flatMap(asHotel)
        } yield {
          assert(notFound.statusText)(equalTo("Not Found")) &&
          assert(loaded)(equalTo(created))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check get list") {
      (for {
        _ <- evalDb("clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          emptyList    <- c.get(baseUri).send(b)
          created      <- c.post(baseUri).body(ApiCreateHotelStarCategory(stars5, HotelStarDescription("5 star Egypt"), egyptRegion).toJson).send(b).flatMap(asHotel)
          nonEmptyList <- c.get(baseUri).send(b).flatMap(asHotelList)
        } yield {
          assert(emptyList.statusText)(equalTo("OK")) &&
          assert(emptyList.body)(equalTo("[]")) && assert(created.stars)(equalTo(stars5)) &&
          assert(nonEmptyList)(equalTo(List(created)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check update") {
      (for {
        _ <- evalDb("clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          notFoundUpdate <-
            c.put(baseUri.addPath("123")).body(ApiUpdateHotelStarCategory(stars5, HotelStarDescription("5 star Egypt Hurghada"), HotelStarRegion("Egypt Hurghada")).toJson).send(b)
          created <- c.post(baseUri).body(ApiCreateHotelStarCategory(stars5, HotelStarDescription("5 star Egypt"), egyptRegion).toJson).send(b).flatMap(asHotel)
          updated <-
            c.put(baseUri.addPath(created.id.toString))
              .body(ApiUpdateHotelStarCategory(stars5, HotelStarDescription("5 star Egypt Hurghada"), HotelStarRegion("Egypt Hurghada")).toJson)
              .send(b)
              .flatMap(asHotel)
        } yield {
          assert(notFoundUpdate.statusText)(equalTo("Not Found")) &&
          assert(updated.stars)(equalTo(stars5)) &&
          assert(updated.description)(equalTo(HotelStarDescription("5 star Egypt Hurghada"))) &&
          assert(updated.region)(equalTo(HotelStarRegion("Egypt Hurghada")))
        }
      }).use(identity).provideLayer(appLayer)
    }
  )

  val baseUri             = uri"http://localhost:8093/api/v1.0/hotel_stars"
  private val stars5      = HotelStars(5)
  private val egyptRegion = HotelStarRegion("Egypt")
  private def asHotel(response: Response[String]): IO[String, ApiHotelStarCategory] =
    as[ApiHotelStarCategory](response)
  private def asHotelList(response: Response[String]): IO[String, List[ApiHotelStarCategory]] =
    as[List[ApiHotelStarCategory]](response)

}
