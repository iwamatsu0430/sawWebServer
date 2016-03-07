package com.saw.webserver.servers

import java.io._
import java.nio.charset.StandardCharsets

import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }
import scalaz._, Scalaz._, effect._, IO._

import com.saw.webserver.models.http._
import com.saw.webserver.models.http.HttpStatus._
import com.saw.webserver.models.Errors
import com.saw.webserver.utils.ByteUtils
import com.saw.webserver.utils.opt.TryOpt._

trait FileServer extends Server with ByteUtils {

  def run: Unit = {
    val io = (findFile(request.header.path) { file =>
      showFile(file)
    } leftMap { error =>
      show404
    } match {
      case \/-(x) => x
      case -\/(x) => x
    }).catchLeft
    io.unsafePerformIO match {
      case \/-(_) =>
      case -\/(e) => logger.log(e)
    }
  }

  def findFile(path: String)(f: File => IO[Unit]): Errors \/ IO[Unit]  = {
    new File(s"public/$path") match { // TODO: 設定ファイル化
      case file if file.isDirectory => findFile(s"${path}index.html")(f) // TODO: 設定ファイル化
      case file if file.exists => f.apply(file).right
      case _ => Errors(s"$path Not Found").left
    }
  }

  def showFile(file: File): IO[Unit] = {
    for {
      _ <- IO { writeStatus(Ok) }
      contentType <- IO {
        val contentType = file.getPath.split('.').toSeq match {
          case heads :+ extention => ContentType.fromExtension(extention)
          case _ => ContentType.Plain
        }
        writeHeader("Content-Type", contentType.code)
        contentType
      }
      _ <- IO {
        val is = new FileInputStream(file) // NOTE: ファイルの存在は確認済み
        Try {
          writeBinaryBody(readFromInputStream(is))
        } andFinally {
          is.close
        }
      }
    } yield ()
  }

  def show404: IO[Unit] = {
    for {
      _ <- IO { writeStatus(NotFound) }
      _ <- IO { writeHeader("Content-Type", ContentType.Html.code) }
      _ <- IO { writeTextBody("<html><head><title>404 Not Found</title></head><body>404 Not Found</body></html>") }
    } yield ()
  }
}
