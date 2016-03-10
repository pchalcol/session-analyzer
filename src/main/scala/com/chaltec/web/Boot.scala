package com.chaltec.web

import akka.actor.ActorSystem
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import reactivemongo.api.MongoDriver
import scala.concurrent.ExecutionContext.Implicits.global

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("session-listener")

  val driver = new MongoDriver(system)
  val connection = driver.connection(List("localhost"), nbChannelsPerNode = 20)
  def db = connection.db("vslsessions")

  // create and start our service actor
  val service = SessionListenerServiceActor()
  val service2 = SessionServiceActor()

  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 9292)
  IO(Http) ? Http.Bind(service2, interface = "localhost", port = 9393)
}
