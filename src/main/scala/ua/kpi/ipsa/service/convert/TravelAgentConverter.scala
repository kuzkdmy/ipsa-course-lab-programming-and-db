package ua.kpi.ipsa.service.convert

import cats.syntax.list._
import ua.kpi.ipsa.domain._
import ua.kpi.ipsa.domain.filter.ListTravelAgentFilter
import ua.kpi.ipsa.domain.types._
import ua.kpi.ipsa.dto._
import ua.kpi.ipsa.service.convert.HotelStarConverter.toApiHotelStarCategory
import ua.kpi.ipsa.service.convert.LocationConverter.toApiLocation

object TravelAgentConverter {
  def toCreateTravelAgent(cmd: ApiCreateTravelAgent): TravelAgentRepr = {
    TravelAgentRepr(
      agent = TravelAgentRow(
        id     = -1L,
        name   = cmd.name.value,
        photos = cmd.photos.map(_.value)
      ),
      locations           = cmd.locations.map(_.value),
      hotelStarCategories = cmd.hotelStarCategories.map(_.value)
    )
  }
  def toUpdateTravelAgent(id: TravelAgentId, cmd: ApiUpdateTravelAgent): TravelAgentRepr = {
    TravelAgentRepr(
      agent               = TravelAgentRow(id.value, cmd.name.value, cmd.photos.map(_.value)),
      locations           = cmd.locations.map(_.value),
      hotelStarCategories = cmd.hotelStarCategories.map(_.value)
    )
  }
  def toListTravelAgentFilter(
      ids: List[TravelAgentId],
      namesIn: List[TravelAgentName],
      countriesIn: List[TravelAgentCountry],
      citiesIn: List[TravelAgentCity],
      photosIn: List[TravelAgentPhoto],
      hotelStarsIn: List[HotelStars],
      queryLimit: Option[QueryLimit]
  ): ListTravelAgentFilter = {
    ListTravelAgentFilter(
      ids          = ids.map(_.value).distinct.toNel,
      namesIn      = namesIn.map(_.value).distinct.toNel,
      countriesIn  = countriesIn.map(_.value).distinct.toNel,
      citiesIn     = citiesIn.map(_.value).distinct.toNel,
      photosIn     = photosIn.map(_.value).distinct.toNel,
      hotelStarsIn = hotelStarsIn.map(_.value).distinct.toNel,
      queryLimit   = queryLimit.map(_.value)
    )
  }
  def toApiTravelAgent(r: TravelAgent): ApiTravelAgent = {
    ApiTravelAgent(
      id                  = TravelAgentId(r.id),
      name                = TravelAgentName(r.name),
      locations           = r.locations.map(toApiLocation).toSet,
      photos              = r.photos.map(TravelAgentPhoto(_)),
      hotelStarCategories = r.hotelStarCategory.map(toApiHotelStarCategory).toSet
    )
  }
}
