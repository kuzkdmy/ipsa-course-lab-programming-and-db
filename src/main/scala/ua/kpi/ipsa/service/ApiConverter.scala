package ua.kpi.ipsa.service

import cats.syntax.list._
import ua.kpi.ipsa.domain._
import ua.kpi.ipsa.domain.filter.{ListHotelCategoriesFilter, ListLocationsFilter, ListTravelAgentFilter}
import ua.kpi.ipsa.dto._
import ua.kpi.ipsa.dto.filter.{ApiListHotelCategoriesFilter, ApiListLocationsFilter, ApiListTravelAgentsFilter}

object ApiConverter {

  def toApiHotelStarCategory(r: HotelStarCategory) = {
    ApiHotelStarCategory(r.id, r.stars, r.description, r.region)
  }
  def toApiLocationType(r: LocationType): ApiLocationType = {
    r match {
      case LocationType.City    => ApiLocationType.City
      case LocationType.Country => ApiLocationType.Country
      case LocationType.Region  => ApiLocationType.Region
    }
  }
  def toLocationType(api: ApiLocationType): LocationType = {
    api match {
      case ApiLocationType.City    => LocationType.City
      case ApiLocationType.Country => LocationType.Country
      case ApiLocationType.Region  => LocationType.Region
    }
  }
  def toApiLocation(r: Location) = {
    ApiLocation(
      id               = r.id,
      name             = r.name,
      locationType     = toApiLocationType(r.locationType),
      parentLocationId = r.parentLocationId
    )
  }
  def toCreateLocation(api: ApiCreateLocation) = {
    Location(
      id               = -1L,
      name             = api.name,
      locationType     = toLocationType(api.locationType),
      parentLocationId = api.parentLocationId
    )
  }
  def toUpdateLocation(id: Long, api: ApiUpdateLocation) = {
    Location(
      id               = id,
      name             = api.name,
      locationType     = toLocationType(api.locationType),
      parentLocationId = api.parentLocationId
    )
  }
  def toCreateTravelAgent(cmd: ApiCreateTravelAgent): TravelAgentRepr = {
    TravelAgentRepr(
      agent = TravelAgentRow(
        id     = -1L,
        name   = cmd.name,
        photos = cmd.photos
      ),
      locations           = cmd.locations,
      hotelStarCategories = cmd.hotelStarCategories
    )
  }
  def toUpdateTravelAgent(id: Long, cmd: ApiUpdateTravelAgent): TravelAgentRepr = {
    TravelAgentRepr(
      agent               = TravelAgentRow(id, cmd.name, cmd.photos),
      locations           = cmd.locations,
      hotelStarCategories = cmd.hotelStarCategories
    )
  }
  def toApiTravelAgent(r: TravelAgentRow, locations: List[Location], hotelStarCategory: List[HotelStarCategory]) = {
    ApiTravelAgent(
      id                = r.id,
      name              = r.name,
      locations         = locations.map(toApiLocation).toSet,
      photos            = r.photos,
      hotelStarCategory = hotelStarCategory.map(toApiHotelStarCategory).toSet
    )
  }
  def toDomainFilter(apiFilter: ApiListLocationsFilter): ListLocationsFilter = {
    ListLocationsFilter(
      ids   = apiFilter.ids.toNel,
      limit = apiFilter.limit
    )
  }
  def toDomainFilter(apiFilter: ApiListHotelCategoriesFilter): ListHotelCategoriesFilter = {
    ListHotelCategoriesFilter(
      ids   = apiFilter.ids.toNel,
      limit = apiFilter.limit
    )
  }
  def toDomainFilter(apiFilter: ApiListTravelAgentsFilter): ListTravelAgentFilter = {
    ListTravelAgentFilter(
      ids   = apiFilter.ids.toNel,
      limit = apiFilter.limit
    )
  }
}
