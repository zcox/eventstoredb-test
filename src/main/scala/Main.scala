package test

import com.eventstore.dbclient._
import scala.jdk.CollectionConverters._
import scala.util.Try

object Main extends App {

  println("Running...")

  val settings = EventStoreDBConnectionString.parse("esdb://127.0.0.1:2113?tls=false")
  val client = EventStoreDBClient.create(settings)

  val currentTimeStream = "current-time"

  val encoder = ToEventData[CurrentTime]
  val decoder = FromResolvedEvent[CurrentTime]

  //append one event to stream
  //https://developers.eventstore.com/clients/grpc/appending-events/
  val event = CurrentTime.now()
  client
    .appendToStream(
      currentTimeStream,
      AppendToStreamOptions.get().expectedRevision(ExpectedRevision.ANY),
      encoder.encode(event)
    )
    .get()

  def log(event: ResolvedEvent): Unit = {
    val e = event.getOriginalEvent()
    println("ResolvedEvent: {")
    println(s"  eventId: ${e.getEventId().toString()}")
    println(s"  eventType: ${e.getEventType()}")
    println(s"  event: ${decoder.decode(event)}")
    println("}")
  }

  //read all events from stream
  //https://developers.eventstore.com/clients/grpc/reading-events/
  val events =
    Try(client.readStream(currentTimeStream, ReadStreamOptions.get().forwards().fromStart()).get())
      .map(_.getEvents().asScala)
      .getOrElse(List.empty)
  println(s"Read ${events.size} events")
  events.foreach(e => log(e))

  client.shutdown()

  println("Done")

}
