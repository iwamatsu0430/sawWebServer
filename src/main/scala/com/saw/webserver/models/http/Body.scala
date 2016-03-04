package com.saw.webserver.models.http

object Body {

  def create(rawBody: String): Option[Body] = {
    None
  }

  def create(rawBody: Array[Byte]): Option[Body] = {
    None
  }
}

trait Body {
  val rawBody: String
}

case class TextBody(rawBody: String) extends Body {
}

case class JsonBody(rawBody: String) extends Body {
}

case class BinaryBody(rawBody: Array[Byte]) {

}
