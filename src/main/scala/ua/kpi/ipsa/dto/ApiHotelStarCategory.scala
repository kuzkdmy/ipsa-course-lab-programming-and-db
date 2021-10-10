package ua.kpi.ipsa.dto

import ua.kpi.ipsa.domain.types._

case class ApiHotelStarCategory(id: HotelStarCategoryId, stars: HotelStars, description: HotelStarDescription, region: HotelStarRegion)
object ApiHotelStarCategory {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiHotelStarCategory] = DeriveJsonDecoder.gen[ApiHotelStarCategory]
  implicit val encoder: JsonEncoder[ApiHotelStarCategory] = DeriveJsonEncoder.gen[ApiHotelStarCategory]
}

case class ApiUpdateHotelStarCategory(stars: HotelStars, description: HotelStarDescription, region: HotelStarRegion)
object ApiUpdateHotelStarCategory {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiUpdateHotelStarCategory] = DeriveJsonDecoder.gen[ApiUpdateHotelStarCategory]
  implicit val encoder: JsonEncoder[ApiUpdateHotelStarCategory] = DeriveJsonEncoder.gen[ApiUpdateHotelStarCategory]
}

case class ApiCreateHotelStarCategory(stars: HotelStars, description: HotelStarDescription, region: HotelStarRegion)
object ApiCreateHotelStarCategory {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiCreateHotelStarCategory] = DeriveJsonDecoder.gen[ApiCreateHotelStarCategory]
  implicit val encoder: JsonEncoder[ApiCreateHotelStarCategory] = DeriveJsonEncoder.gen[ApiCreateHotelStarCategory]
}
