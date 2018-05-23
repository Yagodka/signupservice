package util

import java.util.concurrent.{Future => JFuture}

import scala.concurrent.{Promise, Future => SFuture}
import scala.util.Try


object FutureConverter {
  def toScala[T](jFuture: JFuture[T]): SFuture[T] = {
    val promise = Promise[T]()
    new Thread(() => {
      promise.complete(Try {
        jFuture.get
      })
    }).start()
    promise.future
  }
}
