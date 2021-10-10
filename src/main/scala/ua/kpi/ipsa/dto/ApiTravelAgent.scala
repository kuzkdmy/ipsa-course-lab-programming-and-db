package ua.kpi.ipsa.dto

import ua.kpi.ipsa.domain.types._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ApiTravelAgent(
    id: TravelAgentId,
    name: TravelAgentName,
    locations: Set[ApiLocation],
    photos: List[TravelAgentPhoto],
    hotelStarCategory: Set[ApiHotelStarCategory]
)
object ApiTravelAgent {
  implicit val decoder: JsonDecoder[ApiTravelAgent] = DeriveJsonDecoder.gen[ApiTravelAgent]
  implicit val encoder: JsonEncoder[ApiTravelAgent] = DeriveJsonEncoder.gen[ApiTravelAgent]
}

case class ApiUpdateTravelAgent(
    name: TravelAgentName,
    locations: Set[LocationId],
    photos: List[TravelAgentPhoto],
    hotelStarCategories: Set[HotelStarCategoryId]
)
object ApiUpdateTravelAgent {
  implicit val decoder: JsonDecoder[ApiUpdateTravelAgent] = DeriveJsonDecoder.gen[ApiUpdateTravelAgent]
  implicit val encoder: JsonEncoder[ApiUpdateTravelAgent] = DeriveJsonEncoder.gen[ApiUpdateTravelAgent]
}

case class ApiCreateTravelAgent(
    name: TravelAgentName,
    photos: List[TravelAgentPhoto],
    locations: Set[LocationId],
    hotelStarCategories: Set[HotelStarCategoryId]
)
object ApiCreateTravelAgent {
  implicit val decoder: JsonDecoder[ApiCreateTravelAgent] = DeriveJsonDecoder.gen[ApiCreateTravelAgent]
  implicit val encoder: JsonEncoder[ApiCreateTravelAgent] = DeriveJsonEncoder.gen[ApiCreateTravelAgent]
}
