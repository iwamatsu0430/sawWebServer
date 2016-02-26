package com.saw.webserver.utils.future

import java.lang.Thread

import scala.annotation.tailrec
import scalaz._, Scalaz._

trait LoopableThread[A] extends Thread {

  val threadTask: () => A

  val threadInterval: Int

  val staticResult: Option[A] = None

  var result: Option[A] = None

  var loopAlive: Boolean = true

  def getResult = staticResult.nonEmpty match {
    case true => staticResult
    case _ => result
  }

  def isLoopAlive = loopAlive

  def kill = loopAlive = false

  @tailrec
  final def loop: Unit = {
    result = threadTask.apply.some
    Thread.sleep(threadInterval)
    if (isLoopAlive) {
      loop
    }
  }
}

object MyFuture {

  def apply[A](f: => A): MyFuture[A] = {
    new MyFuture[A] {
      val task = () => f
    }
  }
}

trait MyFuture[A] {

  val task: () => A

  def loop(implicit interval: Int = 10): LoopingMyFuture[A] = {
    LoopingMyFuture.create(task, interval)
  }

  def await(implicit timeout: Int = 1000): A = {
    val thread = new Thread {
      var result: Option[A] = None
      override def run {
        result = Some(task.apply)
      }
    }
    thread.start
    def wait: Unit = {
      thread.join(timeout)
      if (thread.isAlive) {
        wait
      }
    }
    wait
    thread.result.getOrElse(throw new Exception("await intercepted"))
  }
}

object LoopingMyFuture {

  def create[A](task: () => A, interval: Int, staticResult: Option[A] = None) = {
    val bTask = task
    val bInterval = interval
    val bStaticResult = staticResult
    new LoopingMyFuture[A] {
      val task = bTask
      val interval = bInterval
      val thread = new LoopableThread[A] {
         val threadTask: () => A = bTask
         val threadInterval = bInterval
         override val staticResult = bStaticResult
         override def run = loop
      }
      thread.start
    }
  }
}

trait LoopingMyFuture[A] {

  val task: () => A

  val interval: Int

  val thread: LoopableThread[A]

  def isAlive = thread.isLoopAlive

  def kill: LoopingMyFuture[A] = {
    thread.kill
    this
  }

  def awaitWithKill: Option[A] = {
    kill
    thread.join
    thread.result
  }

  // TODO: map, flatMapのコストが高すぎるのでスレッドプール的な処理にする
  def map[B](f: A => B): LoopingMyFuture[B] = {
    kill
    LoopingMyFuture.create(task map f, interval)
  }

  def flatMap[B](f: A => LoopingMyFuture[B]): LoopingMyFuture[B] = {
    kill
    (task map f).apply
  }

  def foreach(f: A => Unit): Unit = {
    f.apply(thread.getResult.getOrElse(throw new Exception("No Result")))
    kill
  }
}
