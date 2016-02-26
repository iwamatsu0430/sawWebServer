package com.saw.webserver

import scala.io.StdIn

import com.saw.webserver.tasks._
import com.saw.webserver.utils.future.MyFuture

object SawWebServer {

  def main(args: Array[String]): Unit = {
    LoggerTask().loop.foreach { implicit logger =>
      ListenerTask.createServerSocket(8081) { listener =>
        // for {
        //   socketStore <- ListenerTask(listener).loop
        //   _ <- ResponseTask(socketStore).loop
        // } yield {
        //   ReadTask().await
        // }
        val listenerFuture = ListenerTask(listener).loop
        val socketStore = listenerFuture.thread.staticResult.get
        val responseFuture = ResponseTask(socketStore).loop
        ReadTask().await
        listenerFuture.kill
        responseFuture.kill
      } leftMap { error =>
        logger.log(error)
      }
      logger.log("Exit")
      logger.printAll
    }
  }
}
