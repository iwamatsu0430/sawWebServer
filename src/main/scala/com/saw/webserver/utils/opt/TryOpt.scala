package com.saw.webserver.utils.opt

import scala.util.Try

object TryOpt {
  implicit class Finally[A](t: Try[A]) {
    def andFinally(f: => Unit): Try[A] = {
      f
      t
    }
    def andFinally(f: Try[A] => Unit): Try[A] = {
      f.apply(t)
      t
    }
  }
}
