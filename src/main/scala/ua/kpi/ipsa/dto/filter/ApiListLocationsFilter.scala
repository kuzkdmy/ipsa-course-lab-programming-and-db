package ua.kpi.ipsa.dto.filter

case class ApiListLocationsFilter(
    ids: List[Long],
    limit: Option[Int]
)
