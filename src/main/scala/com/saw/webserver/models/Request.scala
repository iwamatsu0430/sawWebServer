package com.saw.webserver.models

import java.io._
import java.net.Socket

import scala.annotation.tailrec
import scalaz._, Scalaz._

import com.saw.webserver.models.http._
import com.saw.webserver.utils._

case object Request extends ByteUtils {

  private val newLine = "\r\n"

  def apply(socket: Socket, is: InputStream): Errors \/ Request = {
    val rawRequest = readFromInputStream(is)
    parseHeader((bytesToString(rawRequest).split(newLine + newLine))(0)).map { header =>
      val bodyStartAt = header.getHeaderLength + stringToBytes(newLine + newLine).length
      val bodyEndAt = bodyStartAt + header.getContentLength.getOrElse(0)
      val rawBody = rawRequest.slice(bodyStartAt, bodyEndAt)
      val body = Body.create(header, rawBody)
      Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, header, body)
    }
  }

  def parseHeader(rawHeader: String): Errors \/ Header = {
    rawHeader.split(newLine).toList match {
      case firstLine :: headerLines => {
        firstLine.split(" ").toList match {
          case method :: path :: protocol :: Nil => {
            val headers = headerLines.map(_.split(":").toList).collect {
              case key :: value :: Nil if value.trim.isEmpty => key -> none
              case key :: value :: Nil => key.trim -> value.trim.some
              case key :: Nil => key -> none
            }.toMap
            Header(method, path, protocol, headers, rawHeader).right
          }
          case _ => Errors("First Line Format is Invalid").left
        }
      }
      case _ => Errors("Lines Not Enough").left
    }
  }
}

case class Request(
  address: String,
  port: Int,
  header: Header,
  body: Option[Body] = None
)
