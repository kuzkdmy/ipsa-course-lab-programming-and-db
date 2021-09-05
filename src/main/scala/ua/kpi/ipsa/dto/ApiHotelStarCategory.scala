package ua.kpi.ipsa.dto

case class ApiHotelStarCategory(id: Long, stars: Int, description: String, region: String)
object ApiHotelStarCategory {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiHotelStarCategory] = DeriveJsonDecoder.gen[ApiHotelStarCategory]
  implicit val encoder: JsonEncoder[ApiHotelStarCategory] = DeriveJsonEncoder.gen[ApiHotelStarCategory]
}

case class ApiUpdateHotelStarCategory(stars: Int, description: String, region: String)
object ApiUpdateHotelStarCategory {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiUpdateHotelStarCategory] = DeriveJsonDecoder.gen[ApiUpdateHotelStarCategory]
  implicit val encoder: JsonEncoder[ApiUpdateHotelStarCategory] = DeriveJsonEncoder.gen[ApiUpdateHotelStarCategory]
}

case class ApiCreateHotelStarCategory(stars: Int, description: String, region: String)
object ApiCreateHotelStarCategory {
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
  implicit val decoder: JsonDecoder[ApiCreateHotelStarCategory] = DeriveJsonDecoder.gen[ApiCreateHotelStarCategory]
  implicit val encoder: JsonEncoder[ApiCreateHotelStarCategory] = DeriveJsonEncoder.gen[ApiCreateHotelStarCategory]
}
