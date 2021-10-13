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

import java.time.{LocalDate, LocalDateTime, LocalTime}

object TravelVoucherFunctionalTest extends BaseFunTest {

  override def spec = suite("Locations Management")(
    testM("check create and get single") {
      (for {
        _                     <- evalDb("clean_db.sql")
        _                     <- MainApp.appProgramResource
        implicit0(b: Backend) <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          travelAgent <- createTravelAgent()
          created     <- c.post(baseUri).body(testTravelVoucher(travelAgent.id, users = List(user1, user2)).toJson).send(b).flatMap(asVoucher)
          loaded      <- c.get(baseUri.addPath(s"${created.id}")).send(b).flatMap(asVoucher)
        } yield {
          assert(created.travelAgentId)(equalTo(travelAgent.id)) &&
          assert(created.voucherStartDate)(equalTo(LocalDateTime.of(LocalDate.of(2021, 1, 1), LocalTime.of(10, 1)))) &&
          assert(created.voucherEndDate)(equalTo(LocalDateTime.of(LocalDate.of(2021, 1, 10), LocalTime.MIDNIGHT))) &&
          assert(created.users)(equalTo(List(user1, user2))) &&
          assert(loaded)(equalTo(created))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list(ids)") {
      (for {
        _                     <- evalDb("clean_db.sql")
        _                     <- MainApp.appProgramResource
        implicit0(b: Backend) <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          agent <- createTravelAgent()
          c1    <- c.post(baseUri).body(testTravelVoucher(agent.id).toJson).send(b).flatMap(asVoucher)
          c2    <- c.post(baseUri).body(testTravelVoucher(agent.id).toJson).send(b).flatMap(asVoucher)
          res1  <- c.get(baseUri.addParams("id" -> s"${c1.id.value}")).send(b).flatMap(asVoucherList)
          res2  <- c.get(baseUri.addParams("id" -> s"${c2.id.value}")).send(b).flatMap(asVoucherList)
          res3  <- c.get(baseUri.addParams("id" -> s"${c1.id.value}", "id" -> s"${c2.id.value}")).send(b).flatMap(asVoucherList)
        } yield {
          assert(res1)(equalTo(List(c1))) &&
          assert(res2)(equalTo(List(c2))) &&
          assert(res3)(equalTo(List(c1, c2)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list(agent_id)") {
      (for {
        _                     <- evalDb("clean_db.sql")
        _                     <- MainApp.appProgramResource
        implicit0(b: Backend) <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          agent1 <- createTravelAgent()
          agent2 <- createTravelAgent()
          c1     <- c.post(baseUri).body(testTravelVoucher(agent1.id).toJson).send(b).flatMap(asVoucher)
          c2     <- c.post(baseUri).body(testTravelVoucher(agent2.id).toJson).send(b).flatMap(asVoucher)
          res1   <- c.get(baseUri.addParams("agent_id" -> s"${agent1.id.value}")).send(b).flatMap(asVoucherList)
          res2   <- c.get(baseUri.addParams("agent_id" -> s"${agent2.id.value}")).send(b).flatMap(asVoucherList)
          res3   <- c.get(baseUri.addParams("agent_id" -> s"${agent1.id.value}", "agent_id" -> s"${agent2.id.value}")).send(b).flatMap(asVoucherList)
        } yield {
          assert(res1)(equalTo(List(c1))) &&
          assert(res2)(equalTo(List(c2))) &&
          assert(res3)(equalTo(List(c1, c2)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list(voucher start day)") {
      (for {
        _                     <- evalDb("clean_db.sql")
        _                     <- MainApp.appProgramResource
        implicit0(b: Backend) <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        val v1Date = LocalDateTime.of(LocalDate.of(2001, 1, 1), LocalTime.MIDNIGHT)
        val v2Date = v1Date.plusMonths(10)
        for {
          agent <- createTravelAgent()
          c1    <- c.post(baseUri).body(testTravelVoucher(agent.id, voucherStartDate = v1Date).toJson).send(b).flatMap(asVoucher)
          c2    <- c.post(baseUri).body(testTravelVoucher(agent.id, voucherStartDate = v2Date).toJson).send(b).flatMap(asVoucher)
          res1  <- c.get(baseUri.addParams("start_day" -> s"${c1.voucherStartDate.toLocalDate}")).send(b).flatMap(asVoucherList)
          res2  <- c.get(baseUri.addParams("start_day" -> s"${c2.voucherStartDate.toLocalDate}")).send(b).flatMap(asVoucherList)
          res3  <- c.get(baseUri.addParams("start_day" -> s"${c1.voucherStartDate.toLocalDate}", "start_day" -> s"${c2.voucherStartDate.toLocalDate}")).send(b).flatMap(asVoucherList)
        } yield {
          assert(res1)(equalTo(List(c1))) &&
          assert(res2)(equalTo(List(c2))) &&
          assert(res3)(equalTo(List(c1, c2)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list(voucher start day greater(inclusive(ge greater/equals))") {
      (for {
        _                     <- evalDb("clean_db.sql")
        _                     <- MainApp.appProgramResource
        implicit0(b: Backend) <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        val v1Date = LocalDateTime.of(LocalDate.of(2001, 1, 1), LocalTime.MIDNIGHT)
        val v2Date = v1Date.plusMonths(10)
        for {
          agent <- createTravelAgent()
          c1    <- c.post(baseUri).body(testTravelVoucher(agent.id, voucherStartDate = v1Date).toJson).send(b).flatMap(asVoucher)
          c2    <- c.post(baseUri).body(testTravelVoucher(agent.id, voucherStartDate = v2Date).toJson).send(b).flatMap(asVoucher)
          res1  <- c.get(baseUri.addParams("start_day_ge" -> s"${c1.voucherStartDate.toLocalDate}")).send(b).flatMap(asVoucherList)
          res2  <- c.get(baseUri.addParams("start_day_ge" -> s"${c2.voucherStartDate.toLocalDate}")).send(b).flatMap(asVoucherList)
          res3  <- c.get(baseUri.addParams("start_day_ge" -> s"${c2.voucherStartDate.plusDays(1).toLocalDate}")).send(b).flatMap(asVoucherList)
        } yield {
          assert(res1)(equalTo(List(c1, c2))) &&
          assert(res2)(equalTo(List(c2))) &&
          assert(res3)(equalTo(List.empty))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list(voucher start day less(exclusive)") {
      (for {
        _                     <- evalDb("clean_db.sql")
        _                     <- MainApp.appProgramResource
        implicit0(b: Backend) <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        val v1Date = LocalDateTime.of(LocalDate.of(2001, 1, 1), LocalTime.MIDNIGHT)
        val v2Date = v1Date.plusMonths(10)
        for {
          agent <- createTravelAgent()
          c1    <- c.post(baseUri).body(testTravelVoucher(agent.id, voucherStartDate = v1Date).toJson).send(b).flatMap(asVoucher)
          c2    <- c.post(baseUri).body(testTravelVoucher(agent.id, voucherStartDate = v2Date).toJson).send(b).flatMap(asVoucher)
          res1  <- c.get(baseUri.addParams("start_day_le" -> s"${c1.voucherStartDate.toLocalDate}")).send(b).flatMap(asVoucherList)
          res2  <- c.get(baseUri.addParams("start_day_le" -> s"${c2.voucherStartDate.toLocalDate}")).send(b).flatMap(asVoucherList)
          res3  <- c.get(baseUri.addParams("start_day_le" -> s"${c2.voucherStartDate.plusDays(1).toLocalDate}")).send(b).flatMap(asVoucherList)
        } yield {
          assert(res1)(equalTo(List.empty)) &&
          assert(res2)(equalTo(List(c1))) &&
          assert(res3)(equalTo(List(c1, c2)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list(user last name") {
      (for {
        _                     <- evalDb("clean_db.sql")
        _                     <- MainApp.appProgramResource
        implicit0(b: Backend) <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          agent <- createTravelAgent()
          c1    <- c.post(baseUri).body(testTravelVoucher(agent.id, users = List(user1)).toJson).send(b).flatMap(asVoucher)
          c2    <- c.post(baseUri).body(testTravelVoucher(agent.id, users = List(user2)).toJson).send(b).flatMap(asVoucher)
          res1  <- c.get(baseUri.addParams("user_last_name" -> s"${user1.lastName.value}")).send(b).flatMap(asVoucherList)
          res2  <- c.get(baseUri.addParams("user_last_name" -> s"${user2.lastName.value}")).send(b).flatMap(asVoucherList)
          res3  <- c.get(baseUri.addParams("user_last_name" -> s"${user1.lastName.value}", "user_last_name" -> s"${user2.lastName.value}")).send(b).flatMap(asVoucherList)
        } yield {
          assert(res1)(equalTo(List(c1))) &&
          assert(res2)(equalTo(List(c2))) &&
          assert(res3)(equalTo(List(c1, c2)))
        }
      }).use(identity).provideLayer(appLayer)
    },
    testM("check list(user passport number") {
      (for {
        _                     <- evalDb("clean_db.sql")
        _                     <- MainApp.appProgramResource
        implicit0(b: Backend) <- AsyncHttpClientZioBackend().toManaged_
      } yield {
        for {
          agent <- createTravelAgent()
          c1    <- c.post(baseUri).body(testTravelVoucher(agent.id, users = List(user1)).toJson).send(b).flatMap(asVoucher)
          c2    <- c.post(baseUri).body(testTravelVoucher(agent.id, users = List(user2)).toJson).send(b).flatMap(asVoucher)
          res1  <- c.get(baseUri.addParams("user_passport" -> s"${user1.passportNumber.orNull}")).send(b).flatMap(asVoucherList)
          res2  <- c.get(baseUri.addParams("user_passport" -> s"${user2.passportNumber.orNull}")).send(b).flatMap(asVoucherList)
          res3  <- c.get(baseUri.addParams("user_passport" -> s"${user1.passportNumber.orNull}", "user_passport" -> s"${user2.passportNumber.orNull}")).send(b).flatMap(asVoucherList)
          res4  <- c.get(baseUri.addParams("user_passport" -> s"${user1.passportNumber.orNull}", "user_passport" -> s"${user2.passportNumber.orNull}", "limit" -> "1")).send(b).flatMap(asVoucherList)
        } yield {
          assert(res1)(equalTo(List(c1))) &&
          assert(res2)(equalTo(List(c2))) &&
          assert(res3)(equalTo(List(c1, c2))) &&
          assert(res4)(equalTo(List(c1)))
        }
      }).use(identity).provideLayer(appLayer)
    }
  )

  def createTravelAgent()(implicit b: Backend): Task[ApiTravelAgent] = {
    for {
      region <- c.post(LocationsFunctionalTest.baseUri).body(ApiCreateLocation(LocationName("North America"), Region, None).toJson).send(b).flatMap(as[ApiLocation])
      hotelStar <- c.post(HotelStarsFunctionalTest.baseUri)
                     .body(ApiCreateHotelStarCategory(HotelStars(5), HotelStarDescription("5 star Egypt"), HotelStarRegion("Egypt")).toJson)
                     .send(b)
                     .flatMap(as[ApiHotelStarCategory])
      travelAgent <- c.post(TravelAgentFunctionalTest.baseUri)
                       .body(
                         ApiCreateTravelAgent(
                           name                = TravelAgentName("travel agent 007"),
                           photos              = List(TravelAgentPhoto("photo 1"), TravelAgentPhoto("photo 2")),
                           locations           = Set(region.id),
                           hotelStarCategories = Set(hotelStar.id)
                         ).toJson
                       )
                       .send(b)
                       .flatMap(as[ApiTravelAgent])
    } yield travelAgent
  }

  private val user1 = ApiTravelVoucherUser(
    firstName      = UserFirstName("user first name 1"),
    lastName       = UserLastName("user last name 1"),
    birthDay       = LocalDate.of(2000, 1, 10),
    passportNumber = Some(UserPassportNumber("12345"))
  )
  private val user2 = ApiTravelVoucherUser(
    firstName      = UserFirstName("user first name 2"),
    lastName       = UserLastName("user last name 2"),
    birthDay       = LocalDate.of(2001, 2, 12),
    passportNumber = Some(UserPassportNumber("654321"))
  )
  private def testTravelVoucher(
      travelAgentId: TravelAgentId,
      voucherStartDate: LocalDateTime   = LocalDateTime.of(LocalDate.of(2021, 1, 1), LocalTime.of(10, 1)),
      voucherEndDate: LocalDateTime     = LocalDateTime.of(LocalDate.of(2021, 1, 10), LocalTime.MIDNIGHT),
      users: List[ApiTravelVoucherUser] = List.empty
  ) = ApiCreateTravelVoucher(
    travelAgentId    = travelAgentId,
    voucherStartDate = voucherStartDate,
    voucherEndDate   = voucherEndDate,
    users            = users
  )
  private val baseUri = uri"http://localhost:8093/api/v1.0/travel_voucher"
  def asVoucher(response: Response[String]): Task[ApiTravelVoucher] =
    as[ApiTravelVoucher](response)
  private def asVoucherList(response: Response[String]): Task[List[ApiTravelVoucher]] =
    as[List[ApiTravelVoucher]](response)

}
