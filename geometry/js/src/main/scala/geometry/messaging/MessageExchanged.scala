package geometry.messaging

final case class MessageExchanged(id: String, from: MessageExchanged.Coord, to: MessageExchanged.Coord) {
  def contains(time: Long) = time >= from.timestamp && time <= to.timestamp
  def maxTimestamp         = from.timestamp.max(to.timestamp)
  def minTimestamp         = from.timestamp.min(to.timestamp)
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

object MessageExchanged {
  final case class Coord(name: String, timestamp: Long)
}
