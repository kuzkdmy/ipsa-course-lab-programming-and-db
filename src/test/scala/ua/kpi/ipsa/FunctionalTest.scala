package ua.kpi.ipsa

import doobie.Update0
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.slf4j.LoggerFactory
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.quick._
import sttp.client3.{Response, SttpBackend}
import ua.kpi.ipsa.MainApp.appLayer
import ua.kpi.ipsa.dto.{ApiCreateHotelStarCategory, ApiHotelStarCategory, ApiUpdateHotelStarCategory}
import zio._
import zio.duration.durationInt
import zio.interop.catz._
import zio.json._
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, TestAspect, assert}

import scala.util.Try

object FunctionalTest extends DefaultRunnableSpec {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def aspects: List[TestAspect[Nothing, _root_.zio.test.environment.TestEnvironment, Nothing, Any]] =
    List(TestAspect.timeoutWarning(10.seconds), TestAspect.timeout(20.seconds), TestAspect.sequential)

  type Backend = SttpBackend[Task, ZioStreams with WebSockets]
  private val c = quickRequest

  override def spec = suite("Hotel Stars Management")(
    testM("check create") {
      (for {
        _ <- evalDb("hotel_stars/clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          created <- c.post(uri"http://localhost:8093/api/v1.0/hotel_stars").body(ApiCreateHotelStarCategory(5, "5 star Egypt", "Egypt").toJson).send(b).flatMap(asHotel)
        } yield {
          assert(created.stars)(equalTo(5)) &&
          assert(created.description)(equalTo("5 star Egypt")) &&
          assert(created.region)(equalTo("Egypt"))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check get single") {
      (for {
        _ <- evalDb("hotel_stars/clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          notFound <- c.get(uri"http://localhost:8093/api/v1.0/hotel_stars/123").send(b)
          created  <- c.post(uri"http://localhost:8093/api/v1.0/hotel_stars").body(ApiCreateHotelStarCategory(5, "5 star Egypt", "Egypt").toJson).send(b).flatMap(asHotel)
          loaded   <- c.get(uri"http://localhost:8093/api/v1.0/hotel_stars/${created.id}").send(b).flatMap(asHotel)
        } yield {
          assert(notFound.statusText)(equalTo("Not Found")) &&
          assert(loaded)(equalTo(created))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check get list") {
      (for {
        _ <- evalDb("hotel_stars/clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          emptyList    <- c.get(uri"http://localhost:8093/api/v1.0/hotel_stars").send(b)
          created      <- c.post(uri"http://localhost:8093/api/v1.0/hotel_stars").body(ApiCreateHotelStarCategory(5, "5 star Egypt", "Egypt").toJson).send(b).flatMap(asHotel)
          nonEmptyList <- c.get(uri"http://localhost:8093/api/v1.0/hotel_stars").send(b).flatMap(asHotelList)
        } yield {
          assert(emptyList.statusText)(equalTo("OK")) &&
          assert(emptyList.body)(equalTo("[]")) && assert(created.stars)(equalTo(5)) &&
          assert(nonEmptyList)(equalTo(List(created)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check update") {
      (for {
        _ <- evalDb("hotel_stars/clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          notFoundUpdate <- c.put(uri"http://localhost:8093/api/v1.0/hotel_stars/123").body(ApiUpdateHotelStarCategory(5, "5 star Egypt Hurghada", "Egypt Hurghada").toJson).send(b)
          created        <- c.post(uri"http://localhost:8093/api/v1.0/hotel_stars").body(ApiCreateHotelStarCategory(5, "5 star Egypt", "Egypt").toJson).send(b).flatMap(asHotel)
          updated        <- c.put(uri"http://localhost:8093/api/v1.0/hotel_stars/${created.id}").body(ApiUpdateHotelStarCategory(5, "5 star Egypt Hurghada", "Egypt Hurghada").toJson).send(b).flatMap(asHotel)
        } yield {
          assert(notFoundUpdate.statusText)(equalTo("Not Found")) &&
          assert(updated.stars)(equalTo(5)) &&
          assert(updated.description)(equalTo("5 star Egypt Hurghada")) &&
          assert(updated.region)(equalTo("Egypt Hurghada"))
        }
      }).use(identity).provideLayer(appLayer)
    }
  )

  private def asHotel(response: Response[String]): IO[String, ApiHotelStarCategory] =
    ZIO.fromEither(response.body.fromJson[ApiHotelStarCategory].left.map(_ => s"failed construct from: ${response}"))
  private def asHotelList(response: Response[String]): IO[String, List[ApiHotelStarCategory]] =
    ZIO.fromEither(response.body.fromJson[List[ApiHotelStarCategory]].left.map(_ => s"failed construct from: ${response}"))

  private def evalDb(fileName: String): RManaged[Has[Transactor[Task]], Int] = {
    for {
      rawSql <- loadContent(fileName)
      tx     <- ZIO.service[Transactor[Task]].toManaged_
      res    <- Update0(rawSql, None).run.transact(tx).toManaged_
    } yield res
  }

  private def loadContent(fileName: String): TaskManaged[String] = {
    ZManaged
      .fromAutoCloseable(ZIO(Thread.currentThread().getContextClassLoader.getResourceAsStream(fileName)))
      .flatMap(source => ZManaged.fromTry(Try(scala.io.Source.fromInputStream(source).mkString)))
      .tapError(err => {
        logger.error(s"Failed to load file : $fileName", err)
        ZManaged.fail(err)
      })
  }

}
