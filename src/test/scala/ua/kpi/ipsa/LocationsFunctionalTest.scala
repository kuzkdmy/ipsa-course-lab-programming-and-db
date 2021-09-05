package ua.kpi.ipsa

import sttp.client3.Response
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.quick._
import ua.kpi.ipsa.MainApp.appLayer
import ua.kpi.ipsa.dto.{ApiCreateLocation, ApiLocation, ApiLocationType, ApiUpdateLocation}
import zio._
import zio.json._
import zio.test.Assertion.equalTo
import zio.test.assert

object LocationsFunctionalTest extends BaseFunTest {
  override def spec = suite("Locations Management")(
    testM("check create") {
      (for {
        _ <- evalDb("location/clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          createdRegion  <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("North America", ApiLocationType.Region, None).toJson).send(b).flatMap(asLocation)
          createdCountry <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("USA", ApiLocationType.Country, Some(createdRegion.id)).toJson).send(b).flatMap(asLocation)
          createdCity    <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("New York", ApiLocationType.City, Some(createdCountry.id)).toJson).send(b).flatMap(asLocation)
        } yield {
          assert(createdRegion.name)(equalTo("North America")) &&
          assert(createdRegion.locationType)(equalTo(ApiLocationType.Region)) &&
          assert(createdRegion.parentLocationId.isEmpty)(equalTo(true)) &&
          assert(createdCountry.name)(equalTo("USA")) &&
          assert(createdCountry.locationType)(equalTo(ApiLocationType.Country)) &&
          assert(createdCountry.parentLocationId)(equalTo(Some(createdRegion.id))) &&
          assert(createdCity.name)(equalTo("New York")) &&
          assert(createdCity.locationType)(equalTo(ApiLocationType.City)) &&
          assert(createdCity.parentLocationId)(equalTo(Some(createdCountry.id)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check get single") {
      (for {
        _ <- evalDb("location/clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          notFound <- c.get(uri"http://localhost:8093/api/v1.0/location/123").send(b)
          created  <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("North America", ApiLocationType.Region, None).toJson).send(b).flatMap(asLocation)
          loaded   <- c.get(uri"http://localhost:8093/api/v1.0/location/${created.id}").send(b).flatMap(asLocation)
        } yield {
          assert(notFound.statusText)(equalTo("Not Found")) &&
          assert(loaded)(equalTo(created))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check get list") {
      (for {
        _ <- evalDb("location/clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          emptyList    <- c.get(uri"http://localhost:8093/api/v1.0/location").send(b)
          created      <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("North America", ApiLocationType.Region, None).toJson).send(b).flatMap(asLocation)
          nonEmptyList <- c.get(uri"http://localhost:8093/api/v1.0/location").send(b).flatMap(asLocationsList)
        } yield {
          assert(emptyList.statusText)(equalTo("OK")) &&
          assert(emptyList.body)(equalTo("[]")) &&
          assert(nonEmptyList)(equalTo(List(created)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check update") {
      (for {
        _ <- evalDb("location/clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          notFoundUpdate      <- c.put(uri"http://localhost:8093/api/v1.0/location/123").body(ApiUpdateLocation("North America", ApiLocationType.Region, None).toJson).send(b)
          createdRegion       <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("North America", ApiLocationType.Region, None).toJson).send(b).flatMap(asLocation)
          createdUsa          <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("USA", ApiLocationType.Country, Some(createdRegion.id)).toJson).send(b).flatMap(asLocation)
          createdCanada       <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("Canada", ApiLocationType.Country, Some(createdRegion.id)).toJson).send(b).flatMap(asLocation)
          createdCityInUsa    <- c.post(uri"http://localhost:8093/api/v1.0/location").body(ApiCreateLocation("City", ApiLocationType.City, Some(createdUsa.id)).toJson).send(b).flatMap(asLocation)
          updatedCityInCanada <- c.put(uri"http://localhost:8093/api/v1.0/location/${createdCityInUsa.id}").body(ApiUpdateLocation("City Changed", ApiLocationType.City, Some(createdCanada.id)).toJson).send(b).flatMap(asLocation)
        } yield {
          assert(notFoundUpdate.statusText)(equalTo("Not Found")) &&
          assert(updatedCityInCanada.name)(equalTo("City Changed")) &&
          assert(updatedCityInCanada.locationType)(equalTo(ApiLocationType.City)) &&
          assert(updatedCityInCanada.parentLocationId)(equalTo(Some(createdCanada.id)))
        }
      }).use(identity).provideLayer(appLayer)
    }
  )

  private def asLocation(response: Response[String]): IO[String, ApiLocation] =
    ZIO.fromEither(response.body.fromJson[ApiLocation].left.map(_ => s"failed construct from: ${response}"))
  private def asLocationsList(response: Response[String]): IO[String, List[ApiLocation]] =
    ZIO.fromEither(response.body.fromJson[List[ApiLocation]].left.map(_ => s"failed construct from: ${response}"))

}
