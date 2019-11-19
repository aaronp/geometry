package messaging

import cats.Id

/**
  * The handle to something which supplies the messages
  */
trait MessageApi[F[_]] {
  def query(timeRange: Range): F[MessageBatch]

  def minEventTime(): Long
}

object MessageApi {

  def test() = fixed(TestMessages.testMessages)

  def fixed(data: Seq[MessageRoundTrip]): MessageApi[Id] = new MessageApi[cats.Id] {
    override def query(timeRange: Range) = {
      MessageBatch(timeRange, data.filter(_.inRange(timeRange)))
    }

    override def minEventTime = data.map(_.from.timestamp).min
  }

  object TestMessages {

    val events = List(
      EventData("a", "foo", "bar", 100, "first"),
      EventData("b", "foo", "fizz", 200, "second"),
      EventData("c", "bar", "fizz", 320, "third"),
      EventData("d", "bar", "foo", 400, "fourth"),
      EventData("e", "alpha", "bar", 500, "fifth"),
      EventData("f", "bla", "beta", 510, "1"),
      EventData("g", "alpha", "beta", 520, "data"),
      EventData("h", "alpha", "beta", 550, "data"),
      EventData("i", "foo", "gamma", 650, "data"),
      EventData("j", "foo", "fizz", 775, "data"),
      EventData("k", "bar", "bla", 805, "data"),
      EventData("l", "foo", "fizz", 815, "data")
    ).map(e => e.copy(timestamp = e.timestamp + System.currentTimeMillis()))

    val testMessages: List[MessageRoundTrip] = events.zipWithIndex.map {
      case (event, i) =>
        val latency = i % 3 match {
          case 0 => 50
          case 1 => 220
          case _ => 150
        }
        val to = event.swapAfter(latency)
        MessageRoundTrip(i.toString, MessageRoundTrip.Coord(event.from, event.timestamp), MessageRoundTrip.Coord(to.from, to.timestamp))
    }
  }

}
