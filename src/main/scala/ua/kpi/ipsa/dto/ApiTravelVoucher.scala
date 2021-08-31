package ua.kpi.ipsa.dto

import java.time.LocalDateTime

case class ApiTravelVoucher(
    travelAgent: String,
    tripDate: LocalDateTime,
    travelDays: Int,
    user: List[ApiTravelVoucherUser]
)
object ApiTravelVoucher {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiTravelVoucher] = DeriveJsonDecoder.gen[ApiTravelVoucher]
  implicit val encoder: JsonEncoder[ApiTravelVoucher] = DeriveJsonEncoder.gen[ApiTravelVoucher]
}

case class ApiTravelVoucherUser(id: Long, firstName: String, lastName: String)
object ApiTravelVoucherUser {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiTravelVoucherUser] = DeriveJsonDecoder.gen[ApiTravelVoucherUser]
  implicit val encoder: JsonEncoder[ApiTravelVoucherUser] = DeriveJsonEncoder.gen[ApiTravelVoucherUser]
}
case class ApiTravelVoucherList(
    data: List[ApiTravelVoucherListEntry]
)
object ApiTravelVoucherList {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiTravelVoucherList] = DeriveJsonDecoder.gen[ApiTravelVoucherList]
  implicit val encoder: JsonEncoder[ApiTravelVoucherList] = DeriveJsonEncoder.gen[ApiTravelVoucherList]
}
case class ApiTravelVoucherListEntry(
    travelAgent: String,
    tripDate: LocalDateTime,
    travelDays: Int,
    user: List[Int]
)
object ApiTravelVoucherListEntry {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiTravelVoucherListEntry] = DeriveJsonDecoder.gen[ApiTravelVoucherListEntry]
  implicit val encoder: JsonEncoder[ApiTravelVoucherListEntry] = DeriveJsonEncoder.gen[ApiTravelVoucherListEntry]
}
