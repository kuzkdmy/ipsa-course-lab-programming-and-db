package ua.kpi.ipsa.domain

import derevo.cats.{eqv, order, show}
import derevo.derive
import io.estatico.newtype.macros.newtype
import sttp.tapir.derevo.schema
import zio.json.{JsonDecoder, JsonEncoder}

object types {
  // TODO research on derive for zio-json
  // doobie derive not works correctly for db array types)
  @derive(eqv, show, order, schema) @newtype case class HotelStarCategoryId(value: Long)
  @derive(eqv, show, order, schema) @newtype case class HotelStars(value: Int)
  @derive(eqv, show, order, schema) @newtype case class HotelStarDescription(value: String)
  @derive(eqv, show, order, schema) @newtype case class HotelStarRegion(value: String)
  @derive(eqv, show, order, schema) @newtype case class LocationId(value: Long)
  @derive(eqv, show, order, schema) @newtype case class LocationName(value: String)
  @derive(eqv, show, order, schema) @newtype case class TravelAgentId(value: Long)
  @derive(eqv, show, order, schema) @newtype case class TravelAgentName(value: String)
  @derive(eqv, show, order, schema) @newtype case class TravelAgentPhoto(value: String)
  @derive(eqv, show, order, schema) @newtype case class TravelVoucherId(value: Long)
  @derive(eqv, show, order, schema) @newtype case class UserId(value: Long)
  @derive(eqv, show, order, schema) @newtype case class UserFirstName(value: String)
  @derive(eqv, show, order, schema) @newtype case class UserLastName(value: String)
  @derive(eqv, show, order, schema) @newtype case class QueryLimit(value: Int)

  object HotelStarCategoryId {
    implicit val decoder: JsonDecoder[HotelStarCategoryId] = JsonDecoder[Long].map(HotelStarCategoryId(_))
    implicit val encoder: JsonEncoder[HotelStarCategoryId] = JsonEncoder[Long].contramap(_.value)
  }

  object HotelStars {
    implicit val decoder: JsonDecoder[HotelStars] = JsonDecoder[Int].map(HotelStars(_))
    implicit val encoder: JsonEncoder[HotelStars] = JsonEncoder[Int].contramap(_.value)
  }

  object HotelStarDescription {
    implicit val decoder: JsonDecoder[HotelStarDescription] = JsonDecoder[String].map(HotelStarDescription(_))
    implicit val encoder: JsonEncoder[HotelStarDescription] = JsonEncoder[String].contramap(_.value)
  }

  object HotelStarRegion {
    implicit val decoder: JsonDecoder[HotelStarRegion] = JsonDecoder[String].map(HotelStarRegion(_))
    implicit val encoder: JsonEncoder[HotelStarRegion] = JsonEncoder[String].contramap(_.value)
  }

  object LocationId {
    implicit val decoder: JsonDecoder[LocationId] = JsonDecoder[Long].map(LocationId(_))
    implicit val encoder: JsonEncoder[LocationId] = JsonEncoder[Long].contramap(_.value)
  }

  object LocationName {
    implicit val decoder: JsonDecoder[LocationName] = JsonDecoder[String].map(LocationName(_))
    implicit val encoder: JsonEncoder[LocationName] = JsonEncoder[String].contramap(_.value)
  }

  object TravelAgentId {
    implicit val decoder: JsonDecoder[TravelAgentId] = JsonDecoder[Long].map(TravelAgentId(_))
    implicit val encoder: JsonEncoder[TravelAgentId] = JsonEncoder[Long].contramap(_.value)
  }

  object TravelAgentName {
    implicit val decoder: JsonDecoder[TravelAgentName] = JsonDecoder[String].map(TravelAgentName(_))
    implicit val encoder: JsonEncoder[TravelAgentName] = JsonEncoder[String].contramap(_.value)
  }

  object TravelAgentPhoto {
    implicit val decoder: JsonDecoder[TravelAgentPhoto] = JsonDecoder[String].map(TravelAgentPhoto(_))
    implicit val encoder: JsonEncoder[TravelAgentPhoto] = JsonEncoder[String].contramap(_.value)
  }

  object TravelVoucherId {
    implicit val decoder: JsonDecoder[TravelVoucherId] = JsonDecoder[Long].map(TravelVoucherId(_))
    implicit val encoder: JsonEncoder[TravelVoucherId] = JsonEncoder[Long].contramap(_.value)
  }

  object UserId {
    implicit val decoder: JsonDecoder[UserId] = JsonDecoder[Long].map(UserId(_))
    implicit val encoder: JsonEncoder[UserId] = JsonEncoder[Long].contramap(_.value)
  }

  object UserFirstName {
    implicit val decoder: JsonDecoder[UserFirstName] = JsonDecoder[String].map(UserFirstName(_))
    implicit val encoder: JsonEncoder[UserFirstName] = JsonEncoder[String].contramap(_.value)
  }

  object UserLastName {
    implicit val decoder: JsonDecoder[UserLastName] = JsonDecoder[String].map(UserLastName(_))
    implicit val encoder: JsonEncoder[UserLastName] = JsonEncoder[String].contramap(_.value)
  }

  object QueryLimit {
    implicit val decoder: JsonDecoder[QueryLimit] = JsonDecoder[Int].map(QueryLimit(_))
    implicit val encoder: JsonEncoder[QueryLimit] = JsonEncoder[Int].contramap(_.value)
  }
}
