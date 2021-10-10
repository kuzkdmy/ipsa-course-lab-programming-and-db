package ua.kpi.ipsa.dto

import ua.kpi.ipsa.domain.types._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ApiLocation(
    id: LocationId,
    name: LocationName,
    locationType: ApiLocationType,
    parentLocationId: Option[LocationId]
)
object ApiLocation {
  implicit val decoder: JsonDecoder[ApiLocation] = DeriveJsonDecoder.gen[ApiLocation]
  implicit val encoder: JsonEncoder[ApiLocation] = DeriveJsonEncoder.gen[ApiLocation]
}

case class ApiUpdateLocation(
    name: LocationName,
    locationType: ApiLocationType,
    parentLocationId: Option[LocationId]
)
object ApiUpdateLocation {
  implicit val decoder: JsonDecoder[ApiUpdateLocation] = DeriveJsonDecoder.gen[ApiUpdateLocation]
  implicit val encoder: JsonEncoder[ApiUpdateLocation] = DeriveJsonEncoder.gen[ApiUpdateLocation]
}

import enumeratum._

sealed abstract class ApiLocationType(override val entryName: String) extends EnumEntry
object ApiLocationType extends Enum[ApiLocationType] {
  val values = findValues
  case object City extends ApiLocationType("city")
  case object Country extends ApiLocationType("country")
  case object Region extends ApiLocationType("region")

  implicit val decoder: JsonDecoder[ApiLocationType] = DeriveJsonDecoder.gen[ApiLocationType]
  implicit val encoder: JsonEncoder[ApiLocationType] = DeriveJsonEncoder.gen[ApiLocationType]
}

case class ApiCreateLocation(
    name: LocationName,
    locationType: ApiLocationType,
    parentLocationId: Option[LocationId]
)
object ApiCreateLocation {
  implicit val decoder: JsonDecoder[ApiCreateLocation] = DeriveJsonDecoder.gen[ApiCreateLocation]
  implicit val encoder: JsonEncoder[ApiCreateLocation] = DeriveJsonEncoder.gen[ApiCreateLocation]
}
