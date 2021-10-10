package ua.kpi.ipsa.domain

case class TravelAgent(
    id: Long,
    name: String,
    locations: List[Location],
    photos: List[String],
    hotelStarCategory: List[HotelStarCategory]
)
