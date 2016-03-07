package com.saw.webserver.utils

import java.io.InputStream

import scala.annotation.tailrec
import scalaz._, Scalaz._

trait ByteUtils {

  val buffer = 1024 // TODO: 設定ファイル化

  def readFromInputStream(is: InputStream): Array[Byte] = {
    @tailrec
    def readAll(accBytes: Array[Byte] = Array()): Array[Byte] = {
      if (is.available > 0) {
        var bytes = new Array[Byte](buffer)
        is.read(bytes, 0, buffer)
        readAll(accBytes ++: bytes)
      } else {
        accBytes
      }
    }
    readAll()
  }

  def bytesToString(input: Array[Byte], encode: String = "UTF-8"): String = { // TODO: 設定ファイル化
    new String(input, encode)
  }

  def stringToBytes(input: String, encode: String = "UTF-8"): Array[Byte] = { // TODO: 設定ファイル化
    input.getBytes(encode)
  }
}
