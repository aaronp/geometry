package messaging

/**
  * The result of a query from the [[MessageApi]], together with the range used in making the query
  */
final case class MessageBatch(queryRange: Range, results: Seq[MessageRoundTrip])
