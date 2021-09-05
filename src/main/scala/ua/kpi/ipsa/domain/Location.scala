package ua.kpi.ipsa.domain

case class Location(
    id: Long,
    name: String,
    locationType: LocationType,
    parentLocationId: Option[Long]
)
