package messaging

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
  * Each tick (which comes perhaps every few hundred milliseconds)
  * should trigger the rendering/advancement of an HTML5 graph.
  *
  * The tick is stored as the 'currentTimestampUTC'.
  *
  * The timestamp will end up as a request to the [[MessageApi]] which will return a [[MessageBatch]].
  *
  * Once the data is returned we save the response as the 'batch' option and make another request, saving
  * that as the 'nextBatchFuture'
  *
  * The message frame continues to receive tick updates until eventually the 'batch' option doesn't contain the tick.
  *
  * @param currentTimestampUTC the most recent tick received from either a user or a scheduled clock
  * @param batchSize the time range used to pull back messages
  * @param batch the messages returned from an API query, unrelated to the currentTimestampUTC or batchSize
  * @param nextBatchFuture an optimization where the next assumed batch is requested from the [[MessageApi]]
  * @tparam F the effect type of the message api
  */
final case class MessageFrame[F[_]](currentTimestampUTC: Long, batchSize: FiniteDuration, batch: Option[MessageBatch], nextBatchFuture: Option[F[MessageBatch]]) {

  def messagesForTimestamp: Seq[MessageRoundTrip] = {
    batch.fold(Seq.empty[MessageRoundTrip]) { b =>
      val filtered: Seq[MessageRoundTrip] = b.results.filter(_.contains(currentTimestampUTC))
      filtered
    }
  }

  def update(tick: Long, api: MessageApi[F])(implicit eval: MessageFrame.Eval[F]): MessageFrame[F] = {
    batch match {
      case None => swap(tick, api.minEventTime(), api)
      case Some(msgBatch) =>
        if (msgBatch.queryRange.contains(tick)) {
          // this tick is still within our query results
          copy(currentTimestampUTC = tick)
        } else {
          // we need to swap
          swap(tick, msgBatch.queryRange.to, api)
        }
    }
  }

  private def reload(queryFromRange: Long, api: MessageApi[F])(implicit eval: MessageFrame.Eval[F]): MessageFrame[F] = {
    val nextRange  = Range(queryFromRange, queryFromRange + batchSize.toMillis)
    val newBatch   = eval.eval(api.query(nextRange))
    val eagerFetch = api.query(Range(nextRange.to, nextRange.to + batchSize.toMillis))
    copy(batch = Option(newBatch), nextBatchFuture = Option(eagerFetch))
  }
  private def swap(tick: Long, queryFromRange: Long, api: MessageApi[F])(implicit eval: MessageFrame.Eval[F]) = {
    nextBatchFuture match {
      case Some(inFlightResult) =>
        // block on our request
        val nextBatch: MessageBatch = eval.eval(inFlightResult)
        if (nextBatch.queryRange.contains(tick)) {
          // hurrah - our eager fetch worked
          val nextRange = Range(queryFromRange, queryFromRange + batchSize.toMillis)
          copy(batch = Option(nextBatch), nextBatchFuture = Option(api.query(nextRange)))
        } else {
          // cache miss - we need to query for this explicit tick
          reload(tick, api)
        }
      case None =>
        // oops - we've not queried yet... ?
        reload(tick, api)
    }
  }
}

object MessageFrame {

  /**
    * An typeclass which provides an unsafe means to get the value from within a parameterized type
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
}
