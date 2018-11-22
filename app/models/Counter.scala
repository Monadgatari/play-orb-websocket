package models

import play.api.libs.json._

case class Counter(id: Long, x: Int)

object Counter {
  implicit val counterFormat: OFormat[Counter] = Json.format[Counter]
}
