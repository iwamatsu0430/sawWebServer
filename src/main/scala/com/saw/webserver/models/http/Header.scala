package com.saw.webserver.models.http

case class Header(
  method: String,
  path: String,
  protocol: String,
  headers: Map[String, Option[String]]
) {

  def get(key: String): Option[String] = {
    headers.get(key).flatten
  }
}
