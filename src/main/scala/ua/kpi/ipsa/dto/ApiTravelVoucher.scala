package ua.kpi.ipsa.dto

import ua.kpi.ipsa.domain.types._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.LocalDateTime

case class ApiTravelVoucher(
    travelAgent: TravelAgentName,
    tripDate: LocalDateTime,
    travelDays: Int,
    user: List[ApiUser]
)
object ApiTravelVoucher {
  implicit val decoder: JsonDecoder[ApiTravelVoucher] = DeriveJsonDecoder.gen[ApiTravelVoucher]
  implicit val encoder: JsonEncoder[ApiTravelVoucher] = DeriveJsonEncoder.gen[ApiTravelVoucher]
}

case class ApiUser(id: UserId, firstName: UserFirstName, lastName: UserLastName)
object ApiUser {
  implicit val decoder: JsonDecoder[ApiUser] = DeriveJsonDecoder.gen[ApiUser]
  implicit val encoder: JsonEncoder[ApiUser] = DeriveJsonEncoder.gen[ApiUser]
}
case class ApiTravelVoucherList(
    data: List[ApiTravelVoucherListEntry]
)
object ApiTravelVoucherList {
  implicit val decoder: JsonDecoder[ApiTravelVoucherList] = DeriveJsonDecoder.gen[ApiTravelVoucherList]
  implicit val encoder: JsonEncoder[ApiTravelVoucherList] = DeriveJsonEncoder.gen[ApiTravelVoucherList]
}
case class ApiTravelVoucherListEntry(
    travelAgent: String,
    tripDate: LocalDateTime,
    travelDays: Int,
    user: List[UserId]
)
object ApiTravelVoucherListEntry {
  implicit val decoder: JsonDecoder[ApiTravelVoucherListEntry] = DeriveJsonDecoder.gen[ApiTravelVoucherListEntry]
  implicit val encoder: JsonEncoder[ApiTravelVoucherListEntry] = DeriveJsonEncoder.gen[ApiTravelVoucherListEntry]
}
