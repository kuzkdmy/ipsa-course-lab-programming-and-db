package ua.kpi.ipsa.service.convert

import cats.syntax.list._
import ua.kpi.ipsa.domain._
import ua.kpi.ipsa.domain.filter.ListLocationsFilter
import ua.kpi.ipsa.domain.types._
import ua.kpi.ipsa.dto._

object LocationConverter {
  def toLocationType(api: ApiLocationType): LocationType = {
    api match {
      case ApiLocationType.City    => LocationType.City
      case ApiLocationType.Country => LocationType.Country
      case ApiLocationType.Region  => LocationType.Region
    }
  }
  def toCreateLocation(api: ApiCreateLocation): Location = {
    Location(
      id               = -1L,
      name             = api.name.value,
      locationType     = toLocationType(api.locationType),
      parentLocationId = api.parentLocationId.map(_.value)
    )
  }
  def toUpdateLocation(id: LocationId, api: ApiUpdateLocation): Location = {
    Location(
      id               = id.value,
      name             = api.name.value,
      locationType     = toLocationType(api.locationType),
      parentLocationId = api.parentLocationId.map(_.value)
    )
  }
  def toListLocationsFilter(ids: List[LocationId], queryLimit: Option[QueryLimit]): ListLocationsFilter = {
    ListLocationsFilter(
      ids   = ids.map(_.value).toNel,
      limit = queryLimit.map(_.value)
    )
  }
  def toApiLocation(r: Location): ApiLocation = {
    ApiLocation(
      id               = LocationId(r.id),
      name             = LocationName(r.name),
      locationType     = toApiLocationType(r.locationType),
      parentLocationId = r.parentLocationId.map(LocationId(_))
    )
  }
  def toApiLocationType(r: LocationType): ApiLocationType = {
    r match {
      case LocationType.City    => ApiLocationType.City
      case LocationType.Country => ApiLocationType.Country
      case LocationType.Region  => ApiLocationType.Region
    }
  }
}
