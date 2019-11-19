package messaging

import geometry._
import messaging.InFlightRenderedGraph._

/**
  * A representation of the in-flight messages as draw so that we can check mouse-overs for the various nodes/messages
  *
  * @param ctxt
  * @param state
  * @param currentTime
  */
class InFlightRenderedGraph(ctxt: RenderContext, state: MessageState, currentTime: Long) {

  val centerByNodeId: Map[String, Point]     = renderNodes(ctxt)
  val inFlightMessages: Seq[InFlightMessage] = messagePositions(centerByNodeId)
  renderMessages(ctxt, inFlightMessages)

  private val nodeWidth = (ctxt.style.nodeRadius * 1.5).toInt
  private def nodeContains(center: Point, mousePoint: Point) = {
    val boundingBox = Rectangle(center.x.toInt - nodeWidth, center.y.toInt - nodeWidth, center.x.toInt + nodeWidth, center.y.toInt + nodeWidth)
    boundingBox.contains(mousePoint)
  }

  private object debounce {
    private var previousSelection = Option.empty[GraphSelection]
    def clear() = {
      val before = previousSelection
      previousSelection = None
      MouseOver(NoSelection, before)
    }
    def nextSelection(currentSelection: GraphSelection) = {
      val before = previousSelection
      previousSelection = Option(currentSelection)
      MouseOver(currentSelection, before)
    }
  }

  def onMouseMove(mousePoint: Point): MouseOver = {
    val mouseOverId = centerByNodeId.collectFirst {
      case (name, center: Point) if nodeContains(center, mousePoint) => name
    }

    mouseOverId match {
      case Some(nodeId) =>
        debounce.nextSelection(NodeSelection(nodeId))
      case None =>
        val msgOpt: Option[InFlightMessage] = inFlightMessages.find { msg =>
          nodeContains(msg.currentPosition, mousePoint)
        }
        msgOpt match {
          case None           => debounce.clear()
          case Some(inFlight) => debounce.nextSelection(MessageSelection(inFlight.message.id))
        }
    }
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
    state.events.map { e =>
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

    val nodes: Seq[String] = state.allEvents.flatMap(e => List(e.from.name, e.to.name)).distinct
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

object InFlightRenderedGraph {
  final case class MouseOver(selection: GraphSelection, previousSelection: Option[GraphSelection])
  sealed trait GraphSelection
  case object NoSelection                        extends GraphSelection
  case class NodeSelection(nodeId: String)       extends GraphSelection
  case class MessageSelection(messageId: String) extends GraphSelection
}
