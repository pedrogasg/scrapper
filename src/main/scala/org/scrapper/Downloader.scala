package org.scrapper

import akka.actor.Actor
import sys.process._
import java.net.URL
import java.io.File
/**
  * Created by OCTO-PFI on 23/02/17.
  */
class Downloader extends Actor{
  override def receive: Receive = {
    case DownloadFile(url: URL) =>

      val name = url.getFile().replaceAll("/", "_")
      println(s"downloading $name")
      url #> new File(s"/Users/OCTO-PFI/Work/bpce-pdf/pdf/$name") !
  }
}
