package geometry.messaging


import geometry.{HtmlUtils, Interpolate, Point}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.{Canvas, Div}
import scalatags.JsDom.all._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Messages")
object Messages {

  final case class Style(circleRadius: Double, nodeRadius: Double)

  final case class RenderContext(canvas: CanvasRenderingContext2D, style: Style, width: Int, height: Double) {
    def center: Point = Point(width / 2, height / 2)
  }

//  trait Shape {
//    def render(ctxt: RenderContext): Unit
//  }

  case class MessageState(unsorted: Seq[MessageEvent[_]], currentTime: Long) {
    val events             = unsorted.sortBy(_.event.timestamp)
    val nodes: Set[String] = events.map(_.event).flatMap(e => List(e.from, e.to)).toSet

    def render(ctxt: RenderContext): Unit = {
      ctxt.canvas.clearRect(0, 0, ctxt.width, ctxt.height)

      Interpolate.pointsOnCircle(ctxt.center, ctxt.style.circleRadius, nodes.size).foreach {
        case Point(x, y) =>
          ctxt.canvas.beginPath()
          ctxt.canvas.arc(x, y, ctxt.style.nodeRadius, 0, Math.PI * 2)
          ctxt.canvas.stroke()
      }
    }
  }

  case class Controls(initialContext: RenderContext) {
    val timeSlider = input(`type` := "range", min := 1, max := 1000, value := 500).render

    def draw = {
      val pos = timeSlider.valueAsNumber
      HtmlUtils.log(s"value -> ${pos}")
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

    val c = Controls(RenderContext(dd, Style((canvas.width) / 4, 10), canvas.width, canvas.height))
    controlsContainer.innerHTML = ""
    controlsContainer.appendChild(c.render)
    //    dd.beginPath()
    //    dd.moveTo(10, 10)
    //    dd.lineTo(50, 200)
    //    dd.stroke()
  }

}
