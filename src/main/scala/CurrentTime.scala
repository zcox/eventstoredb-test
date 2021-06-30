package test

import io.circe._
import io.circe.generic.semiauto._

case class CurrentTime(
  time: Long
)

object CurrentTime {
  implicit val encoder: Encoder[CurrentTime] = deriveEncoder[CurrentTime]
  implicit val decoder: Decoder[CurrentTime] = deriveDecoder[CurrentTime]

  def now(): CurrentTime = CurrentTime(System.currentTimeMillis())
}
