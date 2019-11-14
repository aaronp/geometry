package geometry.messaging

sealed trait MessageEvent[A] {
  def event: EventData[A]
}

final case class SendEvent[A](override val event: EventData[A]) extends MessageEvent[A]

final case class ReceiveEvent[A](override val event: EventData[A]) extends MessageEvent[A]

final case class EventData[A](id: String, from: String, to: String, timestamp: Long, payload: A) {
  def swap(ts: Long = timestamp): EventData[A] = copy(from = to, to = from, timestamp = ts)
  def swapAfter(latency: Long): EventData[A]   = copy(from = to, to = from, timestamp = timestamp + latency)
}
