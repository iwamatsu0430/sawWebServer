package com.saw.webserver.tasks

import java.net.{ InetSocketAddress, ServerSocket }

import scalaz._, Scalaz._
import scala.util.{ Failure, Success, Try }

import com.saw.webserver.models.{ Errors, SocketStore }
import com.saw.webserver.utils.future.{ LoopingMyFuture, MyFuture }
import com.saw.webserver.utils.logger.Logger

object ListenerTask {

  def createServerSocket[A](port: Int)(f: ServerSocket => A): Errors \/ Unit = {
    val listener = new ServerSocket
    val close: A => Unit = a => listener.close
    Try {
      listener.bind(new InetSocketAddress(port))
    } match {
      case Success(_) => (f map close).apply(listener).right
      case Failure(e) => Errors(e.getLocalizedMessage).left
    }
  }
}

case class ListenerTask(listener: ServerSocket)(implicit logger: Logger) {

  val socketStore: SocketStore = SocketStore()

  def loop: LoopingMyFuture[SocketStore] = {
    logger.log("Server START")
    LoopingMyFuture.create(() => {
      Try {listener.accept} match {
        case Success(socket) => socketStore.add(socket)
        case Failure(e) => {
          logger.log(e)
          socketStore
        }
      }
    }, 10, Some(socketStore))
  }
}
