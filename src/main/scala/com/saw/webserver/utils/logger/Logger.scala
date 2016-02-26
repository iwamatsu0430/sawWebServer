package com.saw.webserver.utils.logger

import java.time.LocalDateTime

object Logger {

  private val lockObject: AnyRef = new AnyRef

  private var logQueue: LogQueue = LogQueue()
}

trait Logger extends LoggerTrait {

  // TODO: Impl
  def debug(message: String): Unit = ???

  def info(message: String): Unit = ???

  def warn(message: String): Unit = ???

  def danger(message: String): Unit = ???

  def log[A](message: A): Unit = {
    log(message.toString)
  }

  def log(message: String): Unit = {
    val newLogQueue = Logger.logQueue.add(message)
    Logger.lockObject.synchronized {
      Logger.logQueue = newLogQueue
    }
  }

  def printAll: Unit = {
    val newLogQueue = Logger.logQueue.pullAll { message =>
      println(s"${LocalDateTime.now}: $message")
    }
    Logger.lockObject.synchronized {
      Logger.logQueue = newLogQueue
    }
  }
}
