package ua.kpi.ipsa

import sttp.client3.Response
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.quick._
import ua.kpi.ipsa.MainApp.appLayer
import ua.kpi.ipsa.domain.types._
import ua.kpi.ipsa.dto.ApiLocationType._
import ua.kpi.ipsa.dto._
import zio._
import zio.json._
import zio.test.Assertion.equalTo
import zio.test.assert

object TravelAgentFunctionalTest extends BaseFunTest {
  override def spec = suite("Locations Management")(
    testM("check create and get single") {
      (for {
        _ <- evalDb("clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          region <- c.post(LocationsFunctionalTest.baseUri).body(ApiCreateLocation(LocationName("North America"), Region, None).toJson).send(b).flatMap(as[ApiLocation])
          hotelStar <- c.post(HotelStarsFunctionalTest.baseUri)
                         .body(ApiCreateHotelStarCategory(HotelStars(5), HotelStarDescription("5 star Egypt"), HotelStarRegion("Egypt")).toJson)
                         .send(b)
                         .flatMap(as[ApiHotelStarCategory])
          created <- c.post(baseUri)
                       .body(
                         ApiCreateTravelAgent(
                           name                = TravelAgentName("travel agent 007"),
                           photos              = List(TravelAgentPhoto("photo 1"), TravelAgentPhoto("photo 2")),
                           locations           = Set(region.id),
                           hotelStarCategories = Set(hotelStar.id)
                         ).toJson
                       )
                       .send(b)
                       .flatMap(asTravelAgent)
          loaded <- c.get(baseUri.addPath(s"${created.id}")).send(b).flatMap(asTravelAgent)
        } yield {
          assert(created.name)(equalTo(TravelAgentName("travel agent 007"))) &&
          assert(created.photos)(equalTo(List(TravelAgentPhoto("photo 1"), TravelAgentPhoto("photo 2")))) &&
          assert(created.locations)(equalTo(Set(region))) &&
          assert(created.hotelStarCategories)(equalTo(Set(hotelStar))) &&
          assert(created)(equalTo(loaded))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list query ids") {
      (for {
        _ <- evalDb("clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          c1 <- c.post(baseUri)
                  .body(
                    ApiCreateTravelAgent(
                      name                = TravelAgentName("travel agent 007"),
                      photos              = List(TravelAgentPhoto("photo 1"), TravelAgentPhoto("photo 2")),
                      locations           = Set.empty,
                      hotelStarCategories = Set.empty
                    ).toJson
                  )
                  .send(b)
                  .flatMap(asTravelAgent)
          c2 <- c.post(baseUri)
                  .body(
                    ApiCreateTravelAgent(
                      name                = TravelAgentName("travel agent backup 007"),
                      photos              = List(TravelAgentPhoto("photo 3")),
                      locations           = Set.empty,
                      hotelStarCategories = Set.empty
                    ).toJson
                  )
                  .send(b)
                  .flatMap(asTravelAgent)
          res1 <- c.get(baseUri.addParams("id" -> s"${c1.id.value}")).send(b).flatMap(asTravelAgentsList)
          res2 <- c.get(baseUri.addParams("id" -> s"${c2.id.value}")).send(b).flatMap(asTravelAgentsList)
          res3 <- c.get(baseUri.addParams("id" -> s"${c1.id.value}", "id" -> s"${c2.id.value}")).send(b).flatMap(asTravelAgentsList)
        } yield {
          assert(res1)(equalTo(List(c1))) &&
          assert(res2)(equalTo(List(c2))) &&
          assert(res3)(equalTo(List(c1, c2)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list query multiple filters") {
      (for {
        _ <- evalDb("clean_db.sql")
        _ <- MainApp.appProgramResource
        b <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          americaRegion <- c.post(LocationsFunctionalTest.baseUri).body(ApiCreateLocation(LocationName("North America"), Region, None).toJson).send(b).flatMap(as[ApiLocation])
          usa           <- c.post(LocationsFunctionalTest.baseUri).body(ApiCreateLocation(LocationName("USA"), Country, Some(americaRegion.id)).toJson).send(b).flatMap(as[ApiLocation])
          canada        <- c.post(LocationsFunctionalTest.baseUri).body(ApiCreateLocation(LocationName("Canada"), Country, Some(americaRegion.id)).toJson).send(b).flatMap(as[ApiLocation])
          newYork       <- c.post(LocationsFunctionalTest.baseUri).body(ApiCreateLocation(LocationName("NewYork"), City, Some(usa.id)).toJson).send(b).flatMap(as[ApiLocation])
          hotelStar <- c.post(HotelStarsFunctionalTest.baseUri)
                         .body(ApiCreateHotelStarCategory(HotelStars(5), HotelStarDescription("5 star Egypt"), HotelStarRegion("Egypt")).toJson)
                         .send(b)
                         .flatMap(as[ApiHotelStarCategory])
          c1 <- c.post(baseUri)
                  .body(
                    ApiCreateTravelAgent(
                      name                = TravelAgentName("travel agent 007"),
                      photos              = List(TravelAgentPhoto("PhoTO 1"), TravelAgentPhoto("photo 2")),
                      locations           = Set(usa.id, canada.id),
                      hotelStarCategories = Set(hotelStar.id)
                    ).toJson
                  )
                  .send(b)
                  .flatMap(asTravelAgent)
          c2 <- c.post(baseUri)
                  .body(
                    ApiCreateTravelAgent(
                      name                = TravelAgentName("travel agent backup 007"),
                      photos              = List(TravelAgentPhoto("photo 3")),
                      locations           = Set(newYork.id),
                      hotelStarCategories = Set(hotelStar.id)
                    ).toJson
                  )
                  .send(b)
                  .flatMap(asTravelAgent)
          res1 <- c.get(baseUri.addParams("country" -> "canada")).send(b).flatMap(asTravelAgentsList)
          res2 <- c.get(baseUri.addParams("city" -> "NewYork")).send(b).flatMap(asTravelAgentsList)
          res3 <- c.get(baseUri.addParams("stars" -> "5")).send(b).flatMap(asTravelAgentsList)
          res4 <- c.get(baseUri.addParams("stars" -> "5", "photo" -> "PHOTO 1")).send(b).flatMap(asTravelAgentsList)
        } yield {
          assert(res1)(equalTo(List(c1))) &&
          assert(res2)(equalTo(List(c2))) &&
          assert(res3)(equalTo(List(c1, c2))) &&
          assert(res4)(equalTo(List(c1)))
        }
      }).use(identity).provideLayer(appLayer)
    }
  )

  val baseUri = uri"http://localhost:8093/api/v1.0/travel_agent"
  def asTravelAgent(response: Response[String]): Task[ApiTravelAgent] =
    as[ApiTravelAgent](response)
  private def asTravelAgentsList(response: Response[String]): Task[List[ApiTravelAgent]] =
    as[List[ApiTravelAgent]](response)

}
