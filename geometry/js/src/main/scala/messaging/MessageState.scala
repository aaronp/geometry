package messaging

case class MessageState(allEvents: Seq[MessageRoundTrip], currentTime: Long) {
  val (events, outOfRangeEvents) = allEvents.partition(_.contains(currentTime))

  def maxTimeStamp = if (allEvents.isEmpty) 0L else allEvents.map(_.maxTimestamp).max
  def minTimeStamp = if (allEvents.isEmpty) 0L else allEvents.map(_.minTimestamp).min

  def render(ctxt: RenderContext): InFlightRenderedGraph = {
    new InFlightRenderedGraph(ctxt, this, currentTime)
  }

}
