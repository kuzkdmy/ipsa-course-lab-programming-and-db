package ua.kpi.ipsa.dto.filter

case class ApiListTravelAgentsFilter(
    ids: List[Long],
    limit: Option[Int]
)
