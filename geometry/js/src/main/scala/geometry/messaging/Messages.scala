package geometry.messaging

import geometry.HtmlUtils
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.scalajs.dom
import org.scalajs.dom.html.Div

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Messages")
object Messages {

  @JSExport
  def render(controlsDivId: String, containerId: String) = {
    val api: MessageApi = MessageApi.test()

    HtmlUtils.log(s"Rendering $controlsDivId and $containerId")
    val canvas = HtmlUtils.canvas(containerId)
    canvas.width = dom.window.outerWidth
    canvas.height = (dom.window.outerHeight * 0.7).toInt

    // 2D. Get it?
    val dd = HtmlUtils.canvas2D(containerId)

    val ctxt: RenderContext = RenderContext(dd, Style(200, 10, 3), canvas.width, canvas.height)


    val controlsContainer: Div = HtmlUtils.divById(controlsDivId)
    renderMessages(controlsContainer, ctxt, messages)
  }

  def renderMessages(controlsContainer: Div, ctxt: RenderContext, api: MessageApi) = {

    api.minEventTime.foreach { minTime =>
      val control = Controls(ctxt, api, minTime)
      controlsContainer.innerHTML = ""
      controlsContainer.appendChild(control.render)
    }


  }
}
