package ua.kpi.ipsa.domain

case class TravelAgentRepr(agent: TravelAgentRow, locations: Set[Long], hotelStarCategories: Set[Long])
case class TravelAgentRow(id: Long, name: String, photos: List[String])
case class TravelAgentQueryListRow(id: Long, name: String, photos: List[String], hotelCategoryId: Long, locationId: Long)
