package test

import com.eventstore.dbclient._
import io.circe._
import io.circe.syntax._
import java.util.UUID
import scala.util.Try
import java.util.Base64
import io.circe.parser

trait ToEventData[A] {
  def encode(a: A): EventData
}

object ToEventData {

  implicit def apply[A: Encoder]: ToEventData[A] =
    new ToEventData[A] {
      override def encode(a: A): EventData =
        EventDataBuilder
          .json(
            UUID.randomUUID(),
            a.getClass().getName(),
            a.asJson.noSpaces.getBytes("UTF-8"),
          )
          .build()
    }
}

trait FromResolvedEvent[A] {
  def decode(e: ResolvedEvent): Try[A]
}

object FromResolvedEvent {

  def base64Decode(b: Array[Byte], charsetName: String): String = {
    //EventStore seems to base64-encode the event data and then surround it with double-quotes
    val base64String = new String(b, charsetName).replaceAll("\"", "")
    new String(Base64.getDecoder().decode(base64String), charsetName)
  }

  implicit def apply[A: Decoder]: FromResolvedEvent[A] =
    new FromResolvedEvent[A] {
      override def decode(e: ResolvedEvent): Try[A] =
        parser.decode[A](base64Decode(e.getOriginalEvent().getEventData(), "UTF-8")).toTry
    }
}
