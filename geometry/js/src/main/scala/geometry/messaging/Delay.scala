package geometry.messaging

import scala.concurrent._
import scala.scalajs.js

object Delay {
  def apply[A](millis: Long, result: A): Future[A] = {
    if (millis > 0) {
      Future.successful(result)
    } else {
      val p = Promise[A]()
      js.timers.setTimeout(millis) {
        p.success(result)
      }
      p.future
    }
  }
}
