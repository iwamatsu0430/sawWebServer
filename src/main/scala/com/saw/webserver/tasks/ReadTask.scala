package com.saw.webserver.tasks

import java.net.ServerSocket

import scala.io.StdIn

import com.saw.webserver.utils.future.MyFuture

case class ReadTask() {

  def await: String = MyFuture {
    def inner: String = {
      StdIn.readLine match {
        case result if result == null => result
        case result => inner
      }
    }
    inner
  }.await
}
