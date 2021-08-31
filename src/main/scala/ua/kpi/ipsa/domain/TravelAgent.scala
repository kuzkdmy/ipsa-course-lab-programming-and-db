package ua.kpi.ipsa.domain

case class TravelAgent(
    name: String,
    country: String,
    city: String,
    photos: List[String],
    hotelStarCategory: Int,
    hotelStarCategoryName: Option[String]
)
