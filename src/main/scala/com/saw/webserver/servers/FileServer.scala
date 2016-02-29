package com.saw.webserver.servers

import java.io._
import java.nio.charset.StandardCharsets

import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }
import scalaz._, Scalaz._, effect._, IO._

import com.saw.webserver.models.http._
import com.saw.webserver.models.http.HttpStatus._
import com.saw.webserver.models.Errors
import com.saw.webserver.utils.opt.TryOpt._

trait FileServer extends Server {

  def run: Unit = {
    val io = (findFile(request.path) { file =>
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
    new File(s"public/$path") match {
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
        Try {
          val is = new FileInputStream(file)
          ContentType.textValues.contains(contentType) match {
            case true => {
              showTextFile(is)
              is
            }
            case _ => {
              showBinaryFile(is)
              is
            }
          }
        } foreach { is =>
          is.close
        }
      }
    } yield ()
  }

  def showTextFile(is: InputStream): Unit = {
    val reader = new BufferedReader(new InputStreamReader(is))
    @tailrec // TODO: 一文字ずつで遅いので一行ずつに変更
    def readAll(acc: String = ""): String = {
      reader.read match {
        case c if c < 0 => acc
        case c => readAll(acc + c.asInstanceOf[Char].toString)
      }
    }
    val text = readAll()
    writeTextBody(text)
  }

  def showBinaryFile(is: InputStream): Unit = {
    val buffer = 1024 // TODO: 設定ファイル化
    @tailrec
    def readAll(accBytes: Array[Byte] = Array()): Array[Byte] = {
      var bytes = new Array[Byte](buffer)
      is.read(bytes, 0, buffer) match {
        case length if length < 0 => accBytes
        case _ => {
          readAll(accBytes ++: bytes)
        }
      }
    }
    writeBinaryBody(readAll())
  }

  def show404: IO[Unit] = {
    for {
      _ <- IO { writeStatus(NotFound) }
      _ <- IO { writeHeader("Content-Type", ContentType.Html.code) }
      _ <- IO { writeTextBody("<html><head><title>404 Not Found</title></head><body>404 Not Found</body></html>") }
    } yield ()
  }
}
