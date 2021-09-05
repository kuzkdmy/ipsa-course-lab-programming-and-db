package ua.kpi.ipsa.domain

import enumeratum.{DoobieEnum, Enum, EnumEntry}

sealed abstract class LocationType(override val entryName: String) extends EnumEntry
object LocationType extends Enum[LocationType] with DoobieEnum[LocationType] {
  val values = findValues
  case object City extends LocationType("city")
  case object Country extends LocationType("country")
  case object Region extends LocationType("region")

}
