package com.saw.webserver.models.http

import com.saw.webserver.models.{ SealedEnum, SealedEnumCompanion }

trait SealedContentType extends SealedEnum[String] {
  // TODO: java.nio.Filesを使う
}

sealed abstract class ContentType(val code: String) extends SealedEnum[String]
object ContentType extends SealedEnumCompanion[ContentType, String] {
  // Text
  case object Plain extends ContentType("text/plain")
  case object Html extends ContentType("text/html")
  case object Css extends ContentType("text/css")

  // Image
  case object Jpeg extends ContentType("image/jpeg")
  case object Png extends ContentType("image/png")
  case object Gif extends ContentType("image/gif")

  // Application
  case object Js extends ContentType("application/javascript")
  case object Json extends ContentType("application/json")

  def values: Seq[ContentType] = Seq(Plain, Html, Css, Jpeg, Json)
  def textValues: Seq[ContentType] = Seq(Plain, Html, Css, Js, Json)
  def binaryValues: Seq[ContentType] = Seq(Jpeg, Png, Gif)

  def apply(code: String): ContentType = valueOfOrError(code)

  def fromExtension(extension: String): ContentType = {
    extension match {
      case "html" | "htm" | "HTML" | "HTM" => Html
      case "css" | "CSS" => Css
      case "jpeg" | "jpg" | "JPG" | "JPEG" => Jpeg
      case "png" | "PNG" => Png
      case "gif" | "GIF" => Gif
      case "js" | "JS" => Js
      case "json" | "JSON" => Json
      case _ => Plain
    }
  }
}
