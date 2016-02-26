package com.saw.webserver.models

import java.net.Socket

case class SocketStore() {

  val lockObject: AnyRef = new AnyRef

  var sockets: scala.collection.mutable.Seq[Socket] = scala.collection.mutable.Seq()

  def add(socket: Socket): SocketStore = lockObject.synchronized {
    sockets = sockets :+ socket
    this
  }

  def exec(f: Socket => Unit): SocketStore = lockObject.synchronized {
    sockets = sockets match {
      case head +: tail => {
        f.apply(head)
        head.close
        tail
      }
      case _ => sockets
    }
    this
  }
}
