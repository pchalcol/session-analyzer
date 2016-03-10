package com.chaltec.web

import scala.concurrent.duration._
import akka.actor._
import akka.util.Timeout
import spray.routing.HttpService
import akka.event.LoggingReceive
import akka.pattern.ask
import play.api.libs.json.JsObject
import scala.util.{Failure, Success}
import spray.http.{StatusCodes, RequestProcessingException}
import spray.httpx.PlayJsonSupport._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * User: pchalcol
 * Date: 06/07/14
 * Time: 01:25
 */
trait SessionService extends HttpService { this: Actor with ActorLogging =>
  implicit val timeout: Timeout = 3.second

  val routes = {
    path("session" / "data") {

      post {
        entity(as[JsObject]) { event =>

          val eventWriter = SessionEventWriterActor()

          onComplete(eventWriter ? event) {
            case Success(err) =>
              log.info(s"${System.currentTimeMillis} : ok")
              complete("ok")

            case Failure(ex) =>
              log.error(ex, s"${System.currentTimeMillis} : ko. Erreur: ${ex.getMessage}")
              failWith(new RequestProcessingException(StatusCodes.ServiceUnavailable))
          }
        }
      }
    }
  }
}

object SessionServiceActor {
  def apply()(implicit system: ActorSystem) =
    system.actorOf(Props(classOf[SessionServiceActor]), "session-service")
}

class SessionServiceActor extends Actor with ActorLogging with SessionService {

  def actorRefFactory = context

  def receive: Actor.Receive = LoggingReceive {
    runRoute { routes }
  }

}


/*trait SessionService extends HttpService { this: Actor =>
  implicit val timeout: Timeout = 1.second // for the actor 'asks'

  val readRoutes = {
    path("session" / Segment) { sessionId =>
      get {
        complete {
          val eventReader = SessionEventReaderActor() // comment gérer le cycle de vie des acteurs ?
          var response = ""
          eventReader ? sessionId onSuccess {
            /*case jsValue: JsArray => response = Json.stringify(jsValue)*/
            /*case jsValue => response = jsValue.asInstanceOf[String]*/
            case jsValue: JsObject => response = Json.stringify(jsValue)
          }
          response
        }
      }
    }
  }
}

object SessionServiceActor {
  def apply()(implicit system: ActorSystem) =
    system.actorOf(Props(classOf[SessionServiceActor]), "session-service")
}

class SessionServiceActor extends Actor with ActorLogging with SessionService {

  def actorRefFactory = context

  def receive: Actor.Receive = LoggingReceive {
    runRoute { readRoutes }
  }

}*/