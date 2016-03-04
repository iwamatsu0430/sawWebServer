package com.saw.webserver.utils

import java.io.InputStream

import scala.annotation.tailrec
import scalaz._, Scalaz._

trait ByteUtils {

  val buffer = 1024 // TODO: 設定ファイル化

  def readFromInputStream(is: InputStream)(f: (Array[Byte], Array[Byte]) => Any \/ Unit): Array[Byte] = {
    @tailrec
    def readAll(accBytes: Array[Byte] = Array()): Array[Byte] = {
      var bytes = new Array[Byte](buffer)
      is.read(bytes, 0, buffer)
      val input = bytes.take(bytes.lastIndexWhere(_ != 0) + 1)
      f.apply(accBytes, input) match {
        case -\/(_) => readAll(accBytes ++: input)
        case \/-(_) => accBytes ++: input
      }
    }
    readAll()
  }
}
