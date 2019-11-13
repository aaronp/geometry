package geometry.messaging

import geometry.{HtmlUtils, Interpolate, LineSegment, Point}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.{Canvas, Div}
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

  case class Controls(initialContext: RenderContext) {
    val timeSlider = input(`type` := "range", min := 1, max := 1000, value := 500).render

    def draw = {
      val pos = timeSlider.valueAsNumber
      MessageState(TestMessages.testMessages, pos.toLong).render(initialContext)
    }
    timeSlider.onchange = _ => draw
    timeSlider.oninput = _ => draw

    def render = {
      MessageState(TestMessages.testMessages, 1).render(initialContext)
      div(timeSlider).render
    }
  }

  @JSExport
  def render(controlsDivId: String, containerId: String) = {
    HtmlUtils.log(s"Rendering $controlsDivId and $containerId")
    //<input type="range" min="1" max="100" value="50">
    val controlsContainer: Div = HtmlUtils.divById(controlsDivId)

    val canvas = HtmlUtils.elmById(containerId) match {
      case c: Canvas => c
    }

    // 2D. Get it?
    val dd: CanvasRenderingContext2D = canvas.getContext("2d") match {
      case value: CanvasRenderingContext2D => value
    }

    canvas.width = dom.window.outerWidth
    canvas.height = dom.window.outerHeight

    val c = Controls(RenderContext(dd, Style((canvas.width) / 4, 10, 3), canvas.width, canvas.height))
    controlsContainer.innerHTML = ""
    controlsContainer.appendChild(c.render)
  }

}
