package test

import com.eventstore.dbclient._
import scala.jdk.CollectionConverters._
import scala.util.Try
import java.util.UUID
import io.circe.syntax._
import io.circe.parser.decode
import java.util.Base64

object Main extends App {

  println("Running...")

  val settings = EventStoreDBConnectionString.parse("esdb://127.0.0.1:2113?tls=false")
  val client = EventStoreDBClient.create(settings)

  val currentTimeStream = "current-time"

  val event = CurrentTime.now()
  val eventData = EventDataBuilder.json(
    UUID.randomUUID(),
    event.getClass().getName(),
    event.asJson.noSpaces.getBytes("UTF-8"),
  ).build()
  client.appendToStream(
    currentTimeStream, 
    AppendToStreamOptions.get().expectedRevision(ExpectedRevision.ANY), 
    eventData
  ).get()

  def base64Decode(b: Array[Byte], charsetName: String): String = {
    //EventStore seems to base64-encode the event data and then surround it with double-quotes
    val base64String = new String(b, charsetName).replaceAll("\"", "")
    new String(Base64.getDecoder().decode(base64String), charsetName)
  }

  def log(event: ResolvedEvent): Unit = {
    val e = event.getOriginalEvent()
    println("ResolvedEvent: {")
    println(s"  eventId: ${e.getEventId().toString()}")
    println(s"  eventType: ${e.getEventType()}")
    val ct = decode[CurrentTime](base64Decode(e.getEventData(), "UTF-8"))
    println(s"  event: $ct")
    println("}")
  }

  val events = Try(client.readStream(currentTimeStream, ReadStreamOptions.get().forwards().fromStart()).get())
    .map(_.getEvents().asScala)
    .getOrElse(List.empty)
  println(s"Read ${events.size} events")
  events.foreach(e => log(e))

  client.shutdown()

  println("Done")

}
