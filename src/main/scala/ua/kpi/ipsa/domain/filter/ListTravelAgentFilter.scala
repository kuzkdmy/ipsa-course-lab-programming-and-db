package ua.kpi.ipsa.domain.filter

import cats.data.NonEmptyList
import derevo.cats.show
import derevo.derive

@derive(show)
case class ListTravelAgentFilter(
    ids: Option[NonEmptyList[Long]]           = None,
    namesIn: Option[NonEmptyList[String]]     = None,
    countriesIn: Option[NonEmptyList[String]] = None,
    citiesIn: Option[NonEmptyList[String]]    = None,
    photosIn: Option[NonEmptyList[String]]    = None,
    hotelStarsIn: Option[NonEmptyList[Int]]   = None,
    queryLimit: Option[Int]                   = None
)
