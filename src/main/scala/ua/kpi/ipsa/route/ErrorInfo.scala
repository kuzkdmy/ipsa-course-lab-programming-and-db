package ua.kpi.ipsa.route
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait ErrorInfo

case class NotFound(message: String) extends ErrorInfo
object NotFound {
  implicit val encoder: JsonEncoder[NotFound] = DeriveJsonEncoder.gen[NotFound]
  implicit val decoder: JsonDecoder[NotFound] = DeriveJsonDecoder.gen[NotFound]
}
case class Conflict(message: String) extends ErrorInfo
object Conflict {
  implicit val encoder: JsonEncoder[Conflict] = DeriveJsonEncoder.gen[Conflict]
  implicit val decoder: JsonDecoder[Conflict] = DeriveJsonDecoder.gen[Conflict]
}
case object NoContent extends ErrorInfo
