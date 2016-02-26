package com.saw.webserver.utils.logger

trait LoggerTrait {

  def debug(message: String)

  def info(message: String)

  def warn(message: String)

  def danger(message: String)

  def log(message: String)

  def printAll: Unit
}
