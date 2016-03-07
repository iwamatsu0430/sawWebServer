package com.saw.webserver.models.http

import scala.util.{ Failure, Success, Try }
import scalaz._, Scalaz._

import com.saw.webserver.utils._

case class Header(
  method: String,
  path: String,
  protocol: String,
  headers: Map[String, Option[String]],
  raw: String
) extends ByteUtils {

  def get(key: String): Option[String] = {
    headers.get(key).flatten
  }

  def getHeaderLength: Int = stringToBytes(raw).length

  def getContentType: Option[ContentType] = {
    get("Content-Type").flatMap { contentTypes =>
      val contentType = contentTypes.split(";")(0)
      Try { ContentType.apply(contentType) } match {
        case Success(v) => v.some
        case Failure(e) => none
      }
    }
  }

  def getContentLength: Option[Int] = {
    get("Content-Length").flatMap { contentLengthStr =>
      Try { contentLengthStr.toInt } match {
        case Success(v: Int) => v.some
        case Failure(e) => none
      }
    }
  }
}
