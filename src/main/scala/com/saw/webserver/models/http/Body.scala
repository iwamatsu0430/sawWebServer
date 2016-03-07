package com.saw.webserver.models.http

import scalaz._, Scalaz._

import com.saw.webserver.models.http.ContentType._
import com.saw.webserver.utils.ByteUtils

case object Body extends ByteUtils {

  def create(header: Header, rawBody: Array[Byte]): Option[Body] = {
    if (rawBody.length > 0) {
      header.getContentType match {
        case Some(Json) => JsonBody(bytesToString(rawBody)).some
        case Some(contentType) if textValues.contains(contentType) => TextBody(bytesToString(rawBody)).some
        case Some(MPFD) => FormBody(rawBody, header).some
        case _ => BinaryBody(rawBody, header).some
      }
    } else {
      none
    }
  }
}

trait Body extends ByteUtils

case class TextBody(rawBody: String) extends Body {
}

case class JsonBody(rawBody: String) extends Body {
}

case class BinaryBody(rawBody: Array[Byte], header: Header) extends Body {
}

case class FormBody(rawBody: Array[Byte], header: Header) extends Body {
  val str = bytesToString(rawBody)
  println(str)
}
