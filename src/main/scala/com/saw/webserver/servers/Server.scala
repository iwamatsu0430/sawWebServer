package com.saw.webserver.servers

import java.io._

import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }

import com.saw.webserver.models.http.{ Body, Header, HttpStatus }
import com.saw.webserver.models.Request
import com.saw.webserver.utils.logger.Logger
import com.saw.webserver.utils._

object Server {
  def apply(inputRequest: Request, inputOs: OutputStream)(implicit inputLogger: Logger): Server = {
    new FileServer {
      val request = inputRequest
      val os = inputOs
      val logger = inputLogger
    }.start
  }
}

trait Server extends ByteUtils {

  val request: Request

  val os: OutputStream

  val logger: Logger

  def run: Unit

  def start: Server = {
    run
    this
  }

  def writeText(str: String): Unit = {
    val bytes = stringToBytes(str)
    os.write(bytes)
  }

  def writeNewLine: Unit = {
    writeText("\r\n")
  }

  def writeStatus(status: HttpStatus): Unit = {
    writeText(s"HTTP/1.1 ${status.code}")
    writeNewLine
  }

  def writeHeader[A](key: String, value: A): Unit = {
    writeText(s"${key}: ${value.toString}")
    writeNewLine
  }

  def writeTextBody[A](body: A): Unit = {
    writeNewLine
    writeText(body.toString)
    os.flush
  }

  def writeBinaryBody(bytes: Array[Byte]): Unit = {
    writeHeader("Content-Length", bytes.length)
    writeNewLine
    os.write(bytes)
    os.flush
  }
}
