package ua.kpi.ipsa.domain.filter

import cats.data.NonEmptyList

import java.time.LocalDate

case class ListTravelVouchersFilter(
    ids: Option[NonEmptyList[Long]]            = None,
    agents: Option[NonEmptyList[Long]]         = None,
    createdAt: Option[NonEmptyList[LocalDate]] = None,
    createdAtGE: Option[LocalDate]             = None,
    createdAtLT: Option[LocalDate]             = None,
    updatedAt: Option[NonEmptyList[LocalDate]] = None,
    updatedAtGE: Option[LocalDate]             = None,
    updatedAtLT: Option[LocalDate]             = None,
    startAt: Option[NonEmptyList[LocalDate]]   = None,
    startAtGE: Option[LocalDate]               = None,
    startAtLT: Option[LocalDate]               = None,
    endAt: Option[NonEmptyList[LocalDate]]     = None,
    endAtGE: Option[LocalDate]                 = None,
    endAtLT: Option[LocalDate]                 = None,
    lastNames: Option[NonEmptyList[String]]    = None,
    passports: Option[NonEmptyList[String]]    = None,
    queryLimit: Option[Int]                    = None
)
