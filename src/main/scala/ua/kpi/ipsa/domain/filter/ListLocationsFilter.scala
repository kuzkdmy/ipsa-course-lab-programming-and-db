package ua.kpi.ipsa.domain.filter

import cats.data.NonEmptyList

case class ListLocationsFilter(
    ids: Option[NonEmptyList[Long]] = None,
    limit: Option[Int]              = None
)
