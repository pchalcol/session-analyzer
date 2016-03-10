package com.chaltec.web

import akka.event.LoggingReceive
import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.JsObject
import java.util.UUID
import scala.util.{Failure, Success}

/**
 * User: pchalcol
 * Date: 06/07/14
 * Time: 00:27
 */
trait SessionEventWriter { this: Actor with ActorLogging =>
  import play.api.libs.json._

  def sessionEventsCollection: JSONCollection = Boot.db.collection[JSONCollection]("sessionEvents")

  private val jsonEnricher = JsPath().json.update(
    __.read[JsObject] map { jsObject =>
      jsObject ++ Json.obj("timestamp" -> System.currentTimeMillis)
    }
  )

  def enrichWithTimestamp(event: JsObject) = event.validate(jsonEnricher).asOpt.get

}

object SessionEventWriterActor {
  def apply()(implicit context: ActorContext) =
    context.actorOf(Props(classOf[SessionEventWriterActor]), s"session-event-writer-${UUID.randomUUID}")
}

class SessionEventWriterActor extends Actor with SessionEventWriter with ActorLogging {

  private var attempts: Int = 0

  def receive = LoggingReceive {

    case event: JsObject =>
      val requester = sender
      val insertedResponse = sessionEventsCollection.insert(enrichWithTimestamp(event))

      insertedResponse onComplete {
        case success @ Success(err) =>
          log.info(s"Insertion completed")
          requester ! success
          context.stop(self)

        case failure @ Failure(t) =>
          if (attempts < 3) {
            log.info(s"Insertion failed. Retrying...")
            attempts += 1
            self ! event
          }
          else {
            log.info(s"Insertion definitely failed. Sending error message to requester...")
            requester ! failure
            context.stop(self)
          }
      }

    case _ => log.error("not implemented")
  }
}
