package ua.kpi.ipsa.domain

import derevo.cats.show
import derevo.{Derivation, NewTypeDerivation, derive}
import io.estatico.newtype.macros.newtype
import magnolia.Magnolia
import sttp.tapir.derevo.schema
import zio.json.{JsonDecoder, JsonEncoder}

object types {
  // doobie derive not works correctly for db array types)
  @derive(show, schema, encoder, decoder) @newtype case class HotelStarCategoryId(value: Long)
  @derive(show, schema, encoder, decoder) @newtype case class HotelStars(value: Int)
  @derive(show, schema, encoder, decoder) @newtype case class HotelStarDescription(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class HotelStarRegion(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class LocationId(value: Long)
  @derive(show, schema, encoder, decoder) @newtype case class LocationName(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class TravelAgentId(value: Long)
  @derive(show, schema, encoder, decoder) @newtype case class TravelAgentName(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class TravelAgentCountry(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class TravelAgentCity(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class TravelAgentPhoto(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class TravelVoucherId(value: Long)
  @derive(show, schema, encoder, decoder) @newtype case class UserId(value: Long)
  @derive(show, schema, encoder, decoder) @newtype case class UserFirstName(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class UserLastName(value: String)
  @derive(show, schema, encoder, decoder) @newtype case class QueryLimit(value: Int)
}

private object encoder extends Derivation[JsonEncoder] with NewTypeDerivation[JsonEncoder] {
  implicit def instance[T]: JsonEncoder[T] = macro Magnolia.gen[T]
}
private object decoder extends Derivation[JsonDecoder] with NewTypeDerivation[JsonDecoder] {
  implicit def instance[T]: JsonDecoder[T] = macro Magnolia.gen[T]
}
