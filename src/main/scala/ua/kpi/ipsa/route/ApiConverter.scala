package ua.kpi.ipsa.route

import ua.kpi.ipsa.domain.HotelStarCategory
import ua.kpi.ipsa.dto.ApiHotelStarCategory

object ApiConverter {
  def toApiHotelStarCategory(r: HotelStarCategory) = {
    ApiHotelStarCategory(r.id, r.stars, r.description, r.region)
  }
}
