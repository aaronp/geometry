package geometry.messaging

import geometry.{HtmlUtils, Point}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas
import scalatags.JsDom.all._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Messages")
object Messages {

  final case class Style(circleRadius: Double, nodeRadius: Double, messageNodeRadius: Double) {
    def colorFor(nodeIndex: Int, totalIndices: Int): String = {
      val of = List("#6b5b95", "#feb236", "#d64161", "#ff7b25", "#a2b9bc", "#b2ad7f", "#878f99", "#6b5b95")
      of(nodeIndex % of.size)
    }
  }

  final case class RenderContext(canvas: CanvasRenderingContext2D, style: Style, width: Int, height: Double) {
    def center: Point = Point(width / 2, height / 2)
  }

  case class Controls(initialContext: RenderContext, messages: Seq[MessageExchanged]) {
    //minTimeStamp

    private var state       = MessageState(messages, 1)
    private var latestGraph = state.render(initialContext)
    private val minTime     = state.minTimeStamp.toInt
    private val maxTime     = state.maxTimeStamp.toInt

    private val timeSlider = input(`type` := "range", min := minTime, max := maxTime, value := minTime, style := "width:100%").render

    private def draw = {
      val pos = timeSlider.valueAsNumber
      state = MessageState(messages, pos.toLong)
      latestGraph = state.render(initialContext)
    }
    timeSlider.onchange = _ => draw
    timeSlider.oninput = _ => draw

    initialContext.canvas.canvas.onmousemove = (event) => {

      latestGraph.onMouseMove(Point(event.pageX, event.pageY))
    }

    def render = {
      div(timeSlider).render
    }
  }

  @JSExport
  def render(controlsDivId: String, containerId: String) = {
    HtmlUtils.log(s"Rendering $controlsDivId and $containerId")

    val canvas: Canvas = HtmlUtils.elmById(containerId) match {
      case c: Canvas => c
    }

    // 2D. Get it?
    val dd: CanvasRenderingContext2D = canvas.getContext("2d") match {
      case value: CanvasRenderingContext2D => value
    }

    canvas.width = dom.window.outerWidth
    canvas.height = dom.window.outerHeight

    val controlsContainer = HtmlUtils.divById(controlsDivId)
    controlsContainer.innerHTML = ""

    val ctxt = RenderContext(dd, Style(200, 10, 3), canvas.width, canvas.height)
    val msgs = TestMessages.testMessages
    val c    = Controls(ctxt, msgs)

    controlsContainer.appendChild(c.render)
  }

}
