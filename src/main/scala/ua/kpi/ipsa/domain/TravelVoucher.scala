package ua.kpi.ipsa.domain

import cats.data.NonEmptyList
import org.postgresql.util.PGobject
import zio.json._
import zio.json.ast.Json

import java.time.{LocalDate, LocalDateTime}

case class TravelVoucher(
    id: Long,
    travelAgentId: Long,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    voucherStartDate: LocalDateTime,
    voucherEndDate: LocalDateTime,
    users: List[TravelVoucherUser]
)

case class TravelVoucherUser(
    firstName: String,
    lastName: String,
    birthDay: LocalDate,
    passportNumber: Option[String]
)
object TravelVoucherUser {

  implicit val decoder: JsonDecoder[TravelVoucherUser] = DeriveJsonDecoder.gen[TravelVoucherUser]
  implicit val encoder: JsonEncoder[TravelVoucherUser] = DeriveJsonEncoder.gen[TravelVoucherUser]

  import doobie._
  implicit val listPut: Put[List[TravelVoucherUser]] = Put.Advanced.array[PGobject](NonEmptyList.of("json[]"), "json").contramap { e =>
    e.map(_.toJson).map(toJsonUnsafe).map(jsonToPgObject).toArray
  }
  implicit val listGet: Get[List[TravelVoucherUser]] = Get.Advanced.array[Object](NonEmptyList.of("json")).map { e =>
    e.map(_.toString)
      .map(toJsonUnsafe)
      .map(_.as[TravelVoucherUser] match {
        case Left(err)  => throw new IllegalStateException(s"not a valid TravelVoucherUser, $err")
        case Right(res) => res
      })
      .toList
  }
  private def jsonToPgObject(a: Json): PGobject = {
    val o = new PGobject
    o.setType("json")
    o.setValue(a.toString)
    o
  }
  private def toJsonUnsafe(v: String): Json = {
    v.fromJson[Json] match {
      case Left(err) => throw new IllegalStateException(s"not a valid json $err")
      case Right(v)  => v
    }
  }
}
