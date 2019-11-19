package messaging

import scala.concurrent.duration.FiniteDuration

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

  def update(tick: Long, api: MessageApi[F])(implicit eval: Eval[F]): MessageFrame[F] = {
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

  private def reload(queryFromRange: Long, api: MessageApi[F])(implicit eval: Eval[F]): MessageFrame[F] = {
    val nextRange  = Range(queryFromRange, queryFromRange + batchSize.toMillis)
    val newBatch   = eval.eval(api.query(nextRange))
    val eagerFetch = api.query(Range(nextRange.to, nextRange.to + batchSize.toMillis))
    copy(currentTimestampUTC = queryFromRange, batch = Option(newBatch), nextBatchFuture = Option(eagerFetch))
  }
  private def swap(tick: Long, queryFromRange: Long, api: MessageApi[F])(implicit eval: Eval[F]) = {
    nextBatchFuture match {
      case Some(inFlightResult) =>
        // block on our request
        val nextBatch: MessageBatch = eval.eval(inFlightResult)
        if (nextBatch.queryRange.contains(tick)) {
          // hurrah - our eager fetch worked
          val nextRange = Range(queryFromRange, queryFromRange + batchSize.toMillis)
          copy(currentTimestampUTC = tick, batch = Option(nextBatch), nextBatchFuture = Option(api.query(nextRange)))
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
