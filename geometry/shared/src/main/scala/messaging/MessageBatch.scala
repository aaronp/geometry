package messaging

final case class MessageBatch(queryRange: Range, results: Seq[MessageRoundTrip])
