package com.saw.webserver.utils.logger

case class LogQueue(queue: Seq[String] = Seq()) {
  def add(message: String): LogQueue =  {
    LogQueue(queue :+ message)
  }
  def pullAll(f: String => Unit): LogQueue = {
    queue.foreach(f)
    LogQueue()
  }
}
