package ua.kpi.ipsa.service.convert

import cats.syntax.list._
import ua.kpi.ipsa.domain._
import ua.kpi.ipsa.domain.filter.ListTravelVouchersFilter
import ua.kpi.ipsa.domain.types._
import ua.kpi.ipsa.dto._

import java.time.LocalDateTime

object TravelVoucherConverter {
  def toTravelVoucherUser(api: ApiTravelVoucherUser): TravelVoucherUser = {
    TravelVoucherUser(
      firstName      = api.firstName.value,
      lastName       = api.lastName.value,
      birthDay       = api.birthDay,
      passportNumber = api.passportNumber.map(_.value)
    )
  }
  def toApiTravelVoucherUser(r: TravelVoucherUser) = {
    ApiTravelVoucherUser(
      firstName      = UserFirstName(r.firstName),
      lastName       = UserLastName(r.lastName),
      birthDay       = r.birthDay,
      passportNumber = r.passportNumber.map(n => UserPassportNumber(n))
    )
  }
  def toCreateTravelVoucher(cmd: ApiCreateTravelVoucher): TravelVoucher = {
    val at = LocalDateTime.now()
    TravelVoucher(
      id               = -1L,
      travelAgentId    = cmd.travelAgentId.value,
      createdAt        = at,
      updatedAt        = at,
      voucherStartDate = cmd.voucherStartDate,
      voucherEndDate   = cmd.voucherEndDate,
      users            = cmd.users.map(toTravelVoucherUser)
    )
  }
  def toListTravelVoucherFilter(
      ids: List[TravelVoucherId],
      agents: List[TravelAgentId],
      createdAt: List[TravelVoucherCreatedDay],
      createdAtGE: Option[TravelVoucherCreatedDay],
      createdAtLT: Option[TravelVoucherCreatedDay],
      updatedAt: List[TravelVoucherUpdatedDay],
      updatedAtGE: Option[TravelVoucherUpdatedDay],
      updatedAtLT: Option[TravelVoucherUpdatedDay],
      startAt: List[TravelVoucherStartDay],
      startAtGE: Option[TravelVoucherStartDay],
      startAtLT: Option[TravelVoucherStartDay],
      endAt: List[TravelVoucherEndDay],
      endAtGE: Option[TravelVoucherEndDay],
      endAtLT: Option[TravelVoucherEndDay],
      lastNames: List[UserLastName],
      passports: List[UserPassportNumber],
      queryLimit: Option[QueryLimit]
  ): ListTravelVouchersFilter =
    ListTravelVouchersFilter(
      ids         = ids.map(_.value).toNel,
      agents      = agents.map(_.value).toNel,
      createdAt   = createdAt.map(_.value).toNel,
      createdAtGE = createdAtGE.map(_.value),
      createdAtLT = createdAtLT.map(_.value),
      updatedAt   = updatedAt.map(_.value).toNel,
      updatedAtGE = updatedAtGE.map(_.value),
      updatedAtLT = updatedAtLT.map(_.value),
      startAt     = startAt.map(_.value).toNel,
      startAtGE   = startAtGE.map(_.value),
      startAtLT   = startAtLT.map(_.value),
      endAt       = endAt.map(_.value).toNel,
      endAtGE     = endAtGE.map(_.value),
      endAtLT     = endAtLT.map(_.value),
      lastNames   = lastNames.map(_.value).toNel,
      passports   = passports.map(_.value).toNel,
      queryLimit  = queryLimit.map(_.value)
    )
  def toUpdateTravelVoucher(id: types.TravelVoucherId, cmd: ApiUpdateTravelVoucher) =
    TravelVoucher(
      id               = id.value,
      travelAgentId    = cmd.travelAgentId.value,
      createdAt        = LocalDateTime.now(),
      updatedAt        = LocalDateTime.now(),
      voucherStartDate = cmd.voucherStartDate,
      voucherEndDate   = cmd.voucherEndDate,
      users            = cmd.users.map(toTravelVoucherUser)
    )
  def toApiTravelVoucher(r: TravelVoucher) = {
    ApiTravelVoucher(
      id               = TravelVoucherId(r.id),
      travelAgentId    = TravelAgentId(r.travelAgentId),
      createdAt        = r.createdAt,
      updatedAt        = r.updatedAt,
      voucherStartDate = r.voucherStartDate,
      voucherEndDate   = r.voucherEndDate,
      users            = r.users.map(toApiTravelVoucherUser)
    )
  }
}
