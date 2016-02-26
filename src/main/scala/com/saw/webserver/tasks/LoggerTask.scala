package com.saw.webserver.tasks

import com.saw.webserver.utils.future.LoopingMyFuture
import com.saw.webserver.utils.logger.Logger

case class LoggerTask() {

  val logger = new Logger {}

  def loop: LoopingMyFuture[Logger] = {
    logger.log("Logger Start")
    LoopingMyFuture.create(() => {
      logger.printAll
      logger
    }, 100, Some(logger))
  }
}
