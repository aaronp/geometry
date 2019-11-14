package geometry.messaging

import geometry.HtmlUtils
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Messages")
object Messages {

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
