package geometry.messaging

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
  )

  val testMessages: List[MessageExchanged] = events.zipWithIndex.map {
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
