package ua.kpi.ipsa.service.convert

import cats.syntax.list._
import ua.kpi.ipsa.domain._
import ua.kpi.ipsa.domain.filter.ListHotelCategoriesFilter
import ua.kpi.ipsa.domain.types._
import ua.kpi.ipsa.dto._

object HotelStarConverter {
  def toCreateHotelStarCategory(cmd: ApiCreateHotelStarCategory): HotelStarCategory =
    HotelStarCategory(-1, cmd.stars.value, cmd.description.value, cmd.region.value)
  def toListHotelStarCategoryFilter(ids: List[HotelStarCategoryId], queryLimit: Option[QueryLimit]): ListHotelCategoriesFilter =
    ListHotelCategoriesFilter(
      ids   = ids.map(_.value).toNel,
      limit = queryLimit.map(_.value)
    )
  def toUpdateHotelStarCategory(id: types.HotelStarCategoryId, cmd: ApiUpdateHotelStarCategory) =
    HotelStarCategory(id.value, cmd.stars.value, cmd.description.value, cmd.region.value)
  def toApiHotelStarCategory(r: HotelStarCategory) = {
    ApiHotelStarCategory(
      id          = HotelStarCategoryId(r.id),
      stars       = HotelStars(r.stars),
      description = HotelStarDescription(r.description),
      region      = HotelStarRegion(r.region)
    )
  }
}
