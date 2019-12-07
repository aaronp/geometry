package messaging

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

/**
  * An typeclass which provides an unsafe means to get the value from within a parameterized type
  *
  * @tparam F
  */
trait Eval[F[_]] {
  def eval[A](f: F[A]): A
}

object Eval {
  def apply[F[_]](implicit inst: Eval[F]): Eval[F] = inst
  def forFuture(timeout: FiniteDuration): Eval[Future] = new Eval[Future] {
    override def eval[A](f: Future[A]): A = {
      Await.result(f, timeout)
    }
  }
  implicit object IdInstance extends Eval[cats.Id] {
    override def eval[A](value: cats.Id[A]): A = value
  }
  implicit object TryInstance extends Eval[Try] {
    override def eval[A](value: Try[A]): A = value.get
  }
}
