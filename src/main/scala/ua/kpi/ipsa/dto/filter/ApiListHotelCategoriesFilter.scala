package ua.kpi.ipsa.dto.filter

case class ApiListHotelCategoriesFilter(
    ids: List[Long],
    limit: Option[Int]
)
