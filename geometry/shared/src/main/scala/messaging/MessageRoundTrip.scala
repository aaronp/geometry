package messaging

/**
  * Represents a message with a unique ID and which is known to have been sent and received at particular coords (location and timestamp)
  *
  * @param id
  * @param from
  * @param to
  */
final case class MessageRoundTrip(id: String, from: MessageRoundTrip.Coord, to: MessageRoundTrip.Coord) {
  def inRange(range: Range): Boolean = range.contains(from.timestamp) || range.contains(to.timestamp)
  def contains(time: Long)           = time >= from.timestamp && time <= to.timestamp
  def maxTimestamp                   = from.timestamp.max(to.timestamp)
  def minTimestamp                   = from.timestamp.min(to.timestamp)
  def percentCompleteAt(time: Long): Double = {
    if (time <= from.timestamp) {
      0.0
    } else if (time >= to.timestamp) {
      1.0
    } else {
      (time - from.timestamp).toDouble / took
    }
  }
  def took: Long = to.timestamp - from.timestamp
}

object MessageRoundTrip {
  final case class Coord(name: String, timestamp: Long)
}
