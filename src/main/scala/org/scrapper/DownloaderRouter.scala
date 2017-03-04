package org.scrapper

import akka.actor.{Actor, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

/**
  * Created by OCTO-PFI on 23/02/17.
  */
class DownloaderRouter extends Actor{

  var router = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[Downloader])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }
  override def receive: Receive = {
    case d:DownloadFile =>
      println("send to download")
      router.route(d,sender())
  }
}
