package ua.kpi.ipsa.domain.filter

import cats.data.NonEmptyList

case class ListHotelCategoriesFilter(
    ids: Option[NonEmptyList[Long]] = None,
    limit: Option[Int]              = None
)
