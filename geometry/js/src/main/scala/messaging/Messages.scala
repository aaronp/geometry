package messaging

import geometry.HtmlUtils
import messaging.MessageFrame.Eval
import org.scalajs.dom
import org.scalajs.dom.html.Div

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import monix.execution.Scheduler.Implicits.global

@JSExportTopLevel("Messages")
object Messages {

  @JSExport
  def render(controlsDivId: String, containerId: String) = {

    HtmlUtils.log(s"Rendering $controlsDivId and $containerId")
    val canvas = HtmlUtils.canvas(containerId)
    canvas.width = dom.window.outerWidth
    canvas.height = (dom.window.outerHeight * 0.7).toInt

    // 2D. Get it?
    val dd = HtmlUtils.canvas2D(containerId)

    val ctxt: RenderContext = RenderContext(dd, Style(200, 10, 3), canvas.width, canvas.height)

    val controlsContainer: Div = HtmlUtils.divById(controlsDivId)

    val api = MessageApi.test()
    renderMessages(controlsContainer, ctxt, api)
  }

  def renderMessages[F[_] : Eval](controlsContainer: Div, ctxt: RenderContext, api: MessageApi[F]) = {

    val control = Controls(api)
    controlsContainer.innerHTML = ""
    controlsContainer.appendChild(control.render)

    control.timeOffsets.foreach { msg =>
      HtmlUtils.log(msg.toString)
    }
  }
}
