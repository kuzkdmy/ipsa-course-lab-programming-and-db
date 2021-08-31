package ua.kpi.ipsa.dto

case class ApiTravelAgent(
    name: String,
    country: String,
    city: String,
    photos: List[String],
    hotelStarCategory: Int,
    hotelStarCategoryName: Option[String]
)

object ApiTravelAgent {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiTravelAgent] = DeriveJsonDecoder.gen[ApiTravelAgent]
  implicit val encoder: JsonEncoder[ApiTravelAgent] = DeriveJsonEncoder.gen[ApiTravelAgent]
}
