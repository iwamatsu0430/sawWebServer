package com.saw.webserver.models.http

import com.saw.webserver.models.{ SealedEnum, SealedEnumCompanion }

sealed abstract class HttpStatus(val code: String) extends SealedEnum[String]
object HttpStatus extends SealedEnumCompanion[HttpStatus, String] {
  case object Ok extends HttpStatus("200 OK")
  case object BadRequest extends HttpStatus("400 Bad Request")
  case object NotFound extends HttpStatus("404 Not Found")

  def values: Seq[HttpStatus] = Seq(Ok, BadRequest, NotFound )

  def apply(code: String): HttpStatus = valueOfOrError(code)
}
