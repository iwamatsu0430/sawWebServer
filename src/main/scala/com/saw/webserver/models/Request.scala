package com.saw.webserver.models

import java.io.BufferedReader
import java.net.Socket

import scala.annotation.tailrec
import scalaz._, Scalaz._

case object Request {

  def apply(socket: Socket, reader: BufferedReader): Errors \/ Request = {
    readAll("", reader).split("\r\n").toList match {
      case firstLine :: headerLines => {
        firstLine.split(" ").toList match {
          case method :: path :: protocol :: Nil => {
            val headers = headerLines.map(_.split(":").toList).collect {
              case key :: value :: Nil if value.trim.isEmpty => key -> none
              case key :: value :: Nil => key.trim -> value.trim.some
              case key :: Nil => key -> none
            }.toMap
            Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, method, path, protocol, headers).right
          }
          case _ => Errors("First Line Format is Invalid").left
        }
      }
      case _ => Errors("Lines Not Enough").left
    }
  }

  @tailrec
  def readAll(acc: String, reader: BufferedReader): String = {
    reader.read match {
      case c if c < 0 => acc
      case c => acc + c.asInstanceOf[Char].toString match {
        case str if str.endsWith("\r\n\r\n") => {
          println(str)
          str
        }
        case str => readAll(str, reader)
      }
    }
  }
}

case class Request(
  address: String,
  port: Int,
  method: String,
  path: String,
  protocol: String,
  headers: Map[String, Option[String]]
)
