package com.saw.webserver.models

case class Errors(
  message: String
) {
  override def toString = message
}
