package org.scrapper

import java.net.URL

import akka.actor._


import scala.language.postfixOps

class Supervisor(system: ActorSystem) extends Actor{
  val indexer = context actorOf Props(new Indexer(self))
  val downloader = system.actorOf(Props[DownloaderRouter])
  val maxPages = 1000
  val maxRetries = 2
  var numVisited = 0
  var toScrap = Set.empty[URL]
  var scrapCounts = Map.empty[URL, Int]
  var host2Actor = Map.empty[String, ActorRef]

  def receive: Receive = {
    case Start(url) =>
      println(s"starting $url")
      scrap(url)
    case ScrapFinished(url) =>
      println(s"scraping finished $url")
    case IndexFinished(url, urls) =>
      if (numVisited < maxPages)
        urls.toSet.filter(l => !scrapCounts.contains(l)).foreach(scrap)
      checkAndShutdown(url)
    case DownloadFile(url) =>
      println(s"starting $url")
    case ScrapFailure(url, reason) =>
      val retries: Int = scrapCounts(url)
      println(s"scraping failed $url, $retries, reason = $reason")
      if(retries < maxRetries){
        countVisits(url)
        host2Actor(url.getHost) ! Scrap(url)
      } else
        checkAndShutdown(url)
  }

  def checkAndShutdown(url: URL): Unit = {
    toScrap -= url
    if(toScrap.isEmpty){
      self ! PoisonPill
      system.terminate()
    }
  }

  def scrap(url: URL) = {
    val host = url.getHost
    println(s"host = $host")
    if(!host.isEmpty){
      val actor = host2Actor.getOrElse(host,{
        val buff = system.actorOf(Props(new SiteCrawler(self, indexer, downloader)))
        host2Actor += (host -> buff)
        buff
      })
      numVisited += 1
      toScrap += url
      countVisits(url)
      actor ! Scrap(url)
    }
  }

  def countVisits(url: URL): Unit = scrapCounts += (url -> (scrapCounts.getOrElse(url,0) + 1))
}