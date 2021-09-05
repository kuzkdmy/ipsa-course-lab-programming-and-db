package ua.kpi.ipsa.dto

case class ApiTravelAgent(
    id: Long,
    name: String,
    locations: Set[ApiLocation],
    photos: List[String],
    hotelStarCategory: Set[ApiHotelStarCategory]
)
object ApiTravelAgent {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiTravelAgent] = DeriveJsonDecoder.gen[ApiTravelAgent]
  implicit val encoder: JsonEncoder[ApiTravelAgent] = DeriveJsonEncoder.gen[ApiTravelAgent]
}

case class ApiUpdateTravelAgent(
    name: String,
    locations: Set[Long],
    photos: List[String],
    hotelStarCategories: Set[Long]
)
object ApiUpdateTravelAgent {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiUpdateTravelAgent] = DeriveJsonDecoder.gen[ApiUpdateTravelAgent]
  implicit val encoder: JsonEncoder[ApiUpdateTravelAgent] = DeriveJsonEncoder.gen[ApiUpdateTravelAgent]
}

case class ApiCreateTravelAgent(
    name: String,
    photos: List[String],
    locations: Set[Long],
    hotelStarCategories: Set[Long]
)
object ApiCreateTravelAgent {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiCreateTravelAgent] = DeriveJsonDecoder.gen[ApiCreateTravelAgent]
  implicit val encoder: JsonEncoder[ApiCreateTravelAgent] = DeriveJsonEncoder.gen[ApiCreateTravelAgent]
}
