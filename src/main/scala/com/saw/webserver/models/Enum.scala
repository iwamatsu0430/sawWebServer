package com.saw.webserver.models

trait SealedEnum[+A] {

  val code: A

  val name: String = this.getClass.getSimpleName

  override def toString: String = code.toString
}

trait SealedEnumCompanion[B <: SealedEnum[A], A] {

  def values: Seq[B]

  def valueOf(code: A): Option[B] = values.find(_.code == code)

  def valueOfOrError(code: A, msg: => Option[String] = None): B = {
    valueOf(code).getOrElse(
      throw new IllegalArgumentException(
        msg match {
          case Some(m) => m
          case _ => s"${this.getClass.getName} doesn't contain ${code}"
        }
      )
    )
  }
}
