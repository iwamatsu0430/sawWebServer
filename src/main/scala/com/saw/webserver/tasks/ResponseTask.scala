package com.saw.webserver.tasks

import java.io._
import java.net.{ ServerSocket, Socket }
import java.nio.charset.StandardCharsets

import scala.util.{ Failure, Success, Try }
import scalaz._, Scalaz._

import com.saw.webserver.models._
import com.saw.webserver.servers._
import com.saw.webserver.utils.future.{ LoopingMyFuture, MyFuture }
import com.saw.webserver.utils.logger.Logger
import com.saw.webserver.utils.opt.TryOpt._

case class ResponseTask(socketStore: SocketStore)(implicit logger: Logger) {

  def loop: LoopingMyFuture[Unit] = {
    LoopingMyFuture.create(() => {
      socketStore.exec { socket =>
        usingReaderWriter(socket) { (is, os) =>
          for {
            request <- Request(socket, is)
            server <- Server(request, os).right
          } yield {
            ()
          }
        }
      }
    }, 10)
  }

  def usingReaderWriter(socket: Socket)(f: (InputStream, OutputStream) => Unit): Unit = {
    f.apply(socket.getInputStream, socket.getOutputStream)
  }
}
