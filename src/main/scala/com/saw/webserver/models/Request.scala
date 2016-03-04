package com.saw.webserver.models

import java.io._
import java.net.Socket

import scala.annotation.tailrec
import scalaz._, Scalaz._

import com.saw.webserver.models.http._
import com.saw.webserver.utils._

case object Request extends ByteUtils {

  // TODO バイナリで取得
  def apply(socket: Socket, is: InputStream): Errors \/ Request = {

    var contentLength: Option[Int] = None
    val contentLengthRegex = "Content-Length: (\\d+)".r
    val newLine = "\r\n\r\n".getBytes("UTF-8")
    val rawRequest = readFromInputStream(is) { (acc, input) =>
      println(s"contentLength: $contentLength")
      val merged = acc ++: input
      merged.containsSlice(newLine) match {
        case false => ().left
        case _ => {
          val rawRequest = new String(merged, "UTF-8")
          contentLength = contentLength match {
            case None => {
              contentLengthRegex.findFirstMatchIn(rawRequest) match {
                case Some(length) => length.group(1).toInt.some
                case _ => 0.some
              }
            }
            case _ => contentLength
          }
          contentLength.map { cl =>
            cl <= rawRequest.split("\r\n\r\n").toSeq.tail.mkString("").getBytes("UTF-8").length match {
              case true => ().right
              case _ => ().left
            }
          } getOrElse(().right)
        }
      }
    }
    new String(rawRequest, "UTF-8").split("\r\n\r\n").toSeq match {
      case rawHeader +: bodies => {
        parseHeader(rawHeader).map { header =>
          val rawBody = bodies.mkString("").getBytes("UTF-8")
          Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, header, Body.create(rawBody))
        }
      }
      case _ => Errors("").left
    }

    // val rawRequest = readFromInputStream(is) { (acc, input) =>
    //   val mergedStr = new String(acc ++: input, "UTF-8")
    //   mergedStr.split("\r\n\r\n").toSeq match {
    //     case _ +: Seq() => ().left
    //     case rawHeader +: bodies => {
    //       parseHeader(rawHeader).flatMap { header =>
    //         header.get("Content-Length") match {
    //           case Some(contentLength) => {
    //             contentLength.toInt <= bodies.mkString("").getBytes("UTF-8").length match {
    //               case true => ().right
    //               case _ => ().left
    //             }
    //           }
    //           case _ => ().right
    //         }
    //       }
    //     }
    //   }
    // }
    // new String(rawRequest, "UTF-8").split("\r\n\r\n").toSeq match {
    //   case rawHeader +: bodies => {
    //     parseHeader(rawHeader).map { header =>
    //       val rawBody = bodies.mkString("").trim
    //       rawBody match {
    //         case "" => Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, header, none)
    //         case _ => Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, header, Body.create(rawBody.getBytes("UTF-8")))
    //       }
    //     }
    //   }
    //   case _ => ???
    // }
    // val rawRequest = new String(readFromInputStream(is), "UTF-8")
    // rawRequest.split("\r\n\r\n").toSeq match {
    //   case rawHeader +: Seq() => {
    //     parseHeader(rawHeader).map { header =>
    //       Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, header, none)
    //     }
    //   }
    //   case rawHeader +: bodies => {
    //     parseHeader(rawHeader).map { header =>
    //       val rawBody = bodies.mkString("").getBytes("UTF-8")
    //       Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, header, Body.create(rawBody))
    //     }
    //   }
    // }

    // parseHeader(readFromInputStream(is)).map { header =>
    //   header.get("Content-Length").map { contentLength =>
    //     val rawBody = readFromInputStream(is, contentLength.toInt.some)
    //     Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, header, Body.create(rawBody))
    //   } getOrElse {
    //     Request(socket.getInetAddress.getHostAddress, socket.getLocalPort, header, none)
    //   }
    // }
  }

  // def parseHeader(rawHeader: Array[Byte]): Errors \/ Header = {
  //   // 設定ファイル化
  //   new String(rawHeader, "UTF-8").split("\r\n").toList match {
  //     case firstLine :: headerLines => {
  //       firstLine.split(" ").toList match {
  //         case method :: path :: protocol :: Nil => {
  //           val headers = headerLines.map(_.split(":").toList).collect {
  //             case key :: value :: Nil if value.trim.isEmpty => key -> none
  //             case key :: value :: Nil => key.trim -> value.trim.some
  //             case key :: Nil => key -> none
  //           }.toMap
  //           Header(method, path, protocol, headers).right
  //         }
  //         case _ => Errors("First Line Format is Invalid").left
  //       }
  //     }
  //     case _ => Errors("Lines Not Enough").left
  //   }
  // }

  def parseHeader(rawHeader: String): Errors \/ Header = {
    rawHeader.split("\r\n").toList match {
      case firstLine :: headerLines => {
        firstLine.split(" ").toList match {
          case method :: path :: protocol :: Nil => {
            val headers = headerLines.map(_.split(":").toList).collect {
              case key :: value :: Nil if value.trim.isEmpty => key -> none
              case key :: value :: Nil => key.trim -> value.trim.some
              case key :: Nil => key -> none
            }.toMap
            Header(method, path, protocol, headers).right
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
