package geometry.messaging

import geometry.{HtmlUtils, Interpolate, LineSegment, Point}
import geometry.messaging.Messages.RenderContext

case class MessageState(allEvents: Seq[MessageExchanged], currentTime: Long) {
  val (events, outOfRangeEvents) = allEvents.partition(_.contains(currentTime))

  HtmlUtils.log(s"${outOfRangeEvents.size} filtered")

  def render(ctxt: RenderContext): Unit = {
    val centerByNodeId = renderNodes(ctxt)
    val inFlight       = messagePositions(centerByNodeId)
    renderMessages(ctxt, inFlight)
  }

  private def renderMessages(ctxt: RenderContext, positions: Seq[InFlightMessage]): Unit = {
    val total = positions.size

    val beforeStyle     = ctxt.canvas.strokeStyle
    val beforeFillStyle = ctxt.canvas.fillStyle

    positions.zipWithIndex.foreach {
      case (inFlight, i) =>
        ctxt.canvas.beginPath()
        val color = ctxt.style.colorFor(i, total)
        ctxt.canvas.strokeStyle = color
        ctxt.canvas.moveTo(inFlight.from.x, inFlight.from.y)
        ctxt.canvas.lineTo(inFlight.currentPosition.x, inFlight.currentPosition.y)

        ctxt.canvas.strokeStyle = "#FFFFFF"
        ctxt.canvas.fillStyle = color

        ctxt.canvas.arc(inFlight.currentPosition.x, inFlight.currentPosition.y, ctxt.style.messageNodeRadius, 0, Math.PI * 2)
        ctxt.canvas.stroke()
        ctxt.canvas.fill()
    }

    ctxt.canvas.strokeStyle = beforeStyle
    ctxt.canvas.fillStyle = beforeFillStyle
  }

  private def messagePositions(centerByNodeId: Map[String, Point]): Seq[InFlightMessage] = {
    events.map { e =>
      val pcnt = e.percentCompleteAt(currentTime)

      val fromPoint = centerByNodeId(e.from.name)
      val toPoint   = centerByNodeId(e.to.name)

      val currentPos = LineSegment(fromPoint, toPoint).scaledPoint(pcnt)
      InFlightMessage(e, fromPoint, toPoint, currentPos)
    }
  }

  private def renderNodes(ctxt: RenderContext): Map[String, Point] = {
    ctxt.canvas.clearRect(0, 0, ctxt.width, ctxt.height)

    val beforeStyle = ctxt.canvas.strokeStyle

//    val nodes: Seq[String] = events.flatMap(e => List(e.from.name, e.to.name)).distinct
    val nodes: Seq[String] = allEvents.flatMap(e => List(e.from.name, e.to.name)).distinct
    val total              = nodes.size

    val centerByNodeId = Interpolate.pointsOnCircle(ctxt.center, ctxt.style.circleRadius, total).zipWithIndex.foldLeft(Map.empty[String, Point]) {
      case (map, (center @ Point(x, y), i)) =>
        ctxt.canvas.beginPath()
        ctxt.canvas.strokeStyle = ctxt.style.colorFor(i, total)
        ctxt.canvas.arc(x, y, ctxt.style.nodeRadius, 0, Math.PI * 2)
        ctxt.canvas.stroke()
        val nodeId = nodes(i)
        map.updated(nodeId, center)
    }
    ctxt.canvas.strokeStyle = beforeStyle
    centerByNodeId
  }
}
