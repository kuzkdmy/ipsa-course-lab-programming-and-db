package ua.kpi.ipsa

import sttp.client3.Response
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.quick._
import ua.kpi.ipsa.MainApp.appLayer
import ua.kpi.ipsa.domain.types.LocationName
import ua.kpi.ipsa.dto.ApiLocationType.{City, Country, Region}
import ua.kpi.ipsa.dto.{ApiCreateLocation, ApiLocation, ApiUpdateLocation}
import zio._
import zio.json._
import zio.test.Assertion.equalTo
import zio.test.assert

object LocationsFunctionalTest extends BaseFunTest {
  override def spec = suite("Locations Management")(
    testM("check create") {
      (for {
        _ <- evalDb("clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          createdRegion  <- c.post(baseUri).body(ApiCreateLocation(LocationName("North America"), Region, None).toJson).send(b).flatMap(asLocation)
          createdCountry <- c.post(baseUri).body(ApiCreateLocation(LocationName("USA"), Country, Some(createdRegion.id)).toJson).send(b).flatMap(asLocation)
          createdCity    <- c.post(baseUri).body(ApiCreateLocation(LocationName("New York"), City, Some(createdCountry.id)).toJson).send(b).flatMap(asLocation)
        } yield {
          assert(createdRegion.name)(equalTo(LocationName("North America"))) &&
          assert(createdRegion.locationType)(equalTo(Region)) &&
          assert(createdRegion.parentLocationId.isEmpty)(equalTo(true)) &&
          assert(createdCountry.name)(equalTo(LocationName("USA"))) &&
          assert(createdCountry.locationType)(equalTo(Country)) &&
          assert(createdCountry.parentLocationId)(equalTo(Some(createdRegion.id))) &&
          assert(createdCity.name)(equalTo(LocationName("New York"))) &&
          assert(createdCity.locationType)(equalTo(City)) &&
          assert(createdCity.parentLocationId)(equalTo(Some(createdCountry.id)))
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
          created  <- c.post(baseUri).body(ApiCreateLocation(LocationName("North America"), Region, None).toJson).send(b).flatMap(asLocation)
          loaded   <- c.get(baseUri.addPath(s"${created.id}")).send(b).flatMap(asLocation)
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
          created      <- c.post(baseUri).body(ApiCreateLocation(LocationName("North America"), Region, None).toJson).send(b).flatMap(asLocation)
          nonEmptyList <- c.get(baseUri).send(b).flatMap(asLocationsList)
        } yield {
          assert(emptyList.statusText)(equalTo("OK")) &&
          assert(emptyList.body)(equalTo("[]")) &&
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
          notFoundUpdate   <- c.put(baseUri.addPath("123")).body(ApiUpdateLocation(LocationName("North America"), Region, None).toJson).send(b)
          createdRegion    <- c.post(baseUri).body(ApiCreateLocation(LocationName("North America"), Region, None).toJson).send(b).flatMap(asLocation)
          createdUsa       <- c.post(baseUri).body(ApiCreateLocation(LocationName("USA"), Country, Some(createdRegion.id)).toJson).send(b).flatMap(asLocation)
          createdCanada    <- c.post(baseUri).body(ApiCreateLocation(LocationName("Canada"), Country, Some(createdRegion.id)).toJson).send(b).flatMap(asLocation)
          createdCityInUsa <- c.post(baseUri).body(ApiCreateLocation(LocationName("City"), City, Some(createdUsa.id)).toJson).send(b).flatMap(asLocation)
          updatedCityInCanada <-
            c.put(baseUri.addPath(createdCityInUsa.id.toString)).body(ApiUpdateLocation(LocationName("City Changed"), City, Some(createdCanada.id)).toJson).send(b).flatMap(asLocation)
        } yield {
          assert(notFoundUpdate.statusText)(equalTo("Not Found")) &&
          assert(updatedCityInCanada.name)(equalTo(LocationName("City Changed"))) &&
          assert(updatedCityInCanada.locationType)(equalTo(City)) &&
          assert(updatedCityInCanada.parentLocationId)(equalTo(Some(createdCanada.id)))
        }
      }).use(identity).provideLayer(appLayer)
    }
  )

  val baseUri = uri"http://localhost:8093/api/v1.0/location"
  private def asLocation(response: Response[String]): IO[String, ApiLocation] =
    as[ApiLocation](response)
  private def asLocationsList(response: Response[String]): IO[String, List[ApiLocation]] =
    as[List[ApiLocation]](response)

}
