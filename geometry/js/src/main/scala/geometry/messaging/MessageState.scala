package geometry.messaging

import geometry.messaging.Messages.RenderContext

case class MessageState(allEvents: Seq[MessageExchanged], currentTime: Long) {
  val (events, outOfRangeEvents) = allEvents.partition(_.contains(currentTime))

  def maxTimeStamp = if (allEvents.isEmpty) 0 else allEvents.map(_.maxTimestamp).max
  def minTimeStamp = if (allEvents.isEmpty) 0 else allEvents.map(_.minTimestamp).min

  def render(ctxt: RenderContext): InFlightRenderedGraph = {
    new InFlightRenderedGraph(ctxt, this, currentTime)
  }

}
