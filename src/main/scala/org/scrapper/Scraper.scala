package org.scrapper

import java.net.URL

import akka.actor.{Actor, ActorRef, Props}
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

import scala.collection.JavaConverters._

class Scraper(indexer: ActorRef, downloader: ActorRef) extends Actor {
  val urlValidator = new UrlValidator()

  def receive: Receive = {
    case Scrap(url) if url.toString().endsWith(".pdf") =>
      println(s"downloading $url")
      downloader ! DownloadFile(url)
    case Scrap(url) =>
      println(s"scraping $url")
      val content = parse(url)
      sender() ! ScrapFinished(url)
      indexer ! Index(url, content)
  }

  def parse(url: URL): Content = {
    val link: String = url.toString
    val host: String = url.getHost
    val protocol: String = url.getProtocol
    val response = Jsoup.connect(link).ignoreContentType(true)
        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1").execute()

    val contentType: String = response.contentType
    if (contentType.startsWith("text/html")) {
      val doc = response.parse()
      val title: String = doc.getElementsByTag("title").asScala.map(e => e.text()).head
      val descriptionTag = doc.getElementsByTag("meta").asScala.filter(e => e.attr("name") == "description")
      val description = if (descriptionTag.isEmpty) "" else descriptionTag.map(e => e.attr("content")).head
      val links: List[URL] = doc.getElementsByTag("a").asScala.map(e => e.attr("href")).filter(s =>
        s.startsWith("/")).map(link => new URL(s"$protocol://$host$link")).toList
      Content(title, description, links)
    } else {
      Content(link, contentType, List())
    }
  }
}