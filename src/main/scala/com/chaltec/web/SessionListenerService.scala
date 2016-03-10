package com.chaltec.web

import spray.routing.HttpService
import akka.actor.{ActorLogging, ActorSystem, Props, Actor}
import akka.event.{Logging, LoggingReceive}
import akka.routing.RoundRobinRouter
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import spray.http.{StatusCodes, RequestProcessingException}

/**
 * User: pchalcol
 * Date: 22/06/14
 * Time: 23:34
 */
trait SessionListenerService extends HttpService { this: Actor with ActorLogging =>
  import spray.httpx.PlayJsonSupport._
  import play.api.libs.json._

  val jsonEnricher = JsPath().json.update(
    __.read[JsObject] map { jsObject =>
      jsObject ++ Json.obj("timestamp" -> System.currentTimeMillis)
    }
  )

  def sessionEventsCollection: JSONCollection = Boot.db.collection[JSONCollection]("sessionEvents")

  def enrichWithTimestamp(event: JsObject) = event.validate(jsonEnricher).asOpt.get

  val sessionRoutes = logRequestResponse("session", Logging.DebugLevel) {
    path("session" / "data") {
      post {
        entity(as[JsObject]) { event =>
          onComplete(sessionEventsCollection.insert(enrichWithTimestamp(event))) {
            case Success(err) =>
              log.debug(s"${System.currentTimeMillis} : ok")
              complete("ok")

            case Failure(ex) =>
              log.debug(s"${System.currentTimeMillis} : ko. Erreur: ${ex.getMessage}")
              failWith(new RequestProcessingException(StatusCodes.ServiceUnavailable))
          }
        }
      }
    }
  }
}

object SessionListenerServiceActor {
  def apply()(implicit system: ActorSystem) = system.actorOf(Props(classOf[SessionListenerServiceActor])
    .withRouter(RoundRobinRouter(nrOfInstances = 500)), "session-listener-service")
}

class SessionListenerServiceActor extends Actor with SessionListenerService with ActorLogging {

  def actorRefFactory = context

  def receive = LoggingReceive { runRoute(sessionRoutes) }
}
