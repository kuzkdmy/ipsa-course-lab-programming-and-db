package ua.kpi.ipsa.derevo

import derevo.{Derivation, NewTypeDerivation}
import magnolia.Magnolia
import zio.json.{JsonDecoder, JsonEncoder}

object encoder extends Derivation[JsonEncoder] with NewTypeDerivation[JsonEncoder] {
  implicit def instance[T]: JsonEncoder[T] = macro Magnolia.gen[T]
}
object decoder extends Derivation[JsonDecoder] with NewTypeDerivation[JsonDecoder] {
  implicit def instance[T]: JsonDecoder[T] = macro Magnolia.gen[T]
}
