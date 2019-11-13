package geometry.messaging

object TestMessages {

  val events = List(
    EventData("a", "foo", "bar", 100, "first"),
    EventData("b", "foo", "fizz", 200, "second"),
    EventData("c", "bar", "fizz", 320, "third"),
    EventData("d", "bar", "foo", 400, "fourth"),
    EventData("e", "alpha", "bar", 500, "fifth")
  )

  val testMessages = events.zipWithIndex.map {
    case (event, i) =>
      val latency = i % 3 match {
        case 0 => 50
        case 1 => 220
        case _ => 150
      }
      val to = event.swapAfter(latency)
      MessageExchanged(i.toString, Coord(event.from, event.timestamp), Coord(to.from, to.timestamp))
  }
}
