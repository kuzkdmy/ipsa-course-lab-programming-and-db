package ua.kpi.ipsa.domain.filter

import cats.data.NonEmptyList

case class ListTravelAgentFilter(
    ids: Option[NonEmptyList[Long]] = None,
    limit: Option[Int]              = None
)
