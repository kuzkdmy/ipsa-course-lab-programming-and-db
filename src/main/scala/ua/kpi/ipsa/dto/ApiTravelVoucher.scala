package ua.kpi.ipsa.dto

import ua.kpi.ipsa.domain.types._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.{LocalDate, LocalDateTime}

case class ApiTravelVoucher(
    id: TravelVoucherId,
    travelAgentId: TravelAgentId,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    voucherStartDate: LocalDateTime,
    voucherEndDate: LocalDateTime,
    users: List[ApiTravelVoucherUser]
)
object ApiTravelVoucher {
  implicit val decoder: JsonDecoder[ApiTravelVoucher] = DeriveJsonDecoder.gen[ApiTravelVoucher]
  implicit val encoder: JsonEncoder[ApiTravelVoucher] = DeriveJsonEncoder.gen[ApiTravelVoucher]
}

case class ApiTravelVoucherUser(
    firstName: UserFirstName,
    lastName: UserLastName,
    birthDay: LocalDate,
    passportNumber: Option[UserPassportNumber]
)
object ApiTravelVoucherUser {
  implicit val decoder: JsonDecoder[ApiTravelVoucherUser] = DeriveJsonDecoder.gen[ApiTravelVoucherUser]
  implicit val encoder: JsonEncoder[ApiTravelVoucherUser] = DeriveJsonEncoder.gen[ApiTravelVoucherUser]
}

case class ApiCreateTravelVoucher(
    travelAgentId: TravelAgentId,
    voucherStartDate: LocalDateTime,
    voucherEndDate: LocalDateTime,
    users: List[ApiTravelVoucherUser]
)
object ApiCreateTravelVoucher {
  implicit val decoder: JsonDecoder[ApiCreateTravelVoucher] = DeriveJsonDecoder.gen[ApiCreateTravelVoucher]
  implicit val encoder: JsonEncoder[ApiCreateTravelVoucher] = DeriveJsonEncoder.gen[ApiCreateTravelVoucher]
}

case class ApiUpdateTravelVoucher(
    travelAgentId: TravelAgentId,
    voucherStartDate: LocalDateTime,
    voucherEndDate: LocalDateTime,
    users: List[ApiTravelVoucherUser]
)
object ApiUpdateTravelVoucher {
  implicit val decoder: JsonDecoder[ApiUpdateTravelVoucher] = DeriveJsonDecoder.gen[ApiUpdateTravelVoucher]
  implicit val encoder: JsonEncoder[ApiUpdateTravelVoucher] = DeriveJsonEncoder.gen[ApiUpdateTravelVoucher]
}
