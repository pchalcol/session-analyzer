package com.chaltec.web

import akka.actor._
import play.modules.reactivemongo.json.collection.JSONCollection
import akka.event.LoggingReceive
import reactivemongo.api.{QueryOpts, Cursor}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee.Iteratee

/**
 * User: pchalcol
 * Date: 06/07/14
 * Time: 00:26
 */
trait SessionEventReader { this: Actor =>
  import play.api.libs.json._

  def sessionEventsCollection: JSONCollection = Boot.db.collection[JSONCollection]("reference")

  def readSessionWithId(sessionId: String) = {
    println(sessionId)

    /*val sessionCursor: Cursor[JsObject] = sessionEventsCollection.find(
      Json.obj("sessionId" -> "4E0BBCFAE2960411844D5B342E9A209B")
    )
      // the cursor must be tailable and await data
      .options(QueryOpts().tailable.awaitData)
      .cursor[JsObject]

    val futureSessionEventsList: Future[List[JsObject]] = sessionCursor.collect[List]()
    val futureSessionEvents: Future[JsArray] = futureSessionEventsList.map { jsObjects =>
      Json.arr(jsObjects)
    }

    futureSessionEvents*/

    val sessionEnumerator = sessionEventsCollection.find(Json.obj("sessionId" -> "4E0BBCFAE2960411844D5B342E9A209B"))
      .cursor[JsObject]
      .enumerate()

    sessionEnumerator.apply(Iteratee.foreach { doc =>
        println(s"found document: $doc")
        sender ! doc
      })

    //Enumerator.flatten(sessionEnumerator)

    "OK"
  }
}

object SessionEventReaderActor {
  def apply()(implicit context: ActorContext) =
    context.actorOf(Props(classOf[SessionEventReaderActor]), "session-event-reader")
}

class SessionEventReaderActor extends Actor with ActorLogging with SessionEventReader {

  def receive: Actor.Receive = LoggingReceive {

    case req: String =>
      log.debug("SessionEventReaderActor::received request")
      sender ! readSessionWithId(req)

    case _ => log.debug("not implemented")
  }
}
