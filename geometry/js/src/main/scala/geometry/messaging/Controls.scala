package geometry.messaging

import geometry.Point
import scalatags.JsDom.all._

/**
  * A handle on the messaging controls
  * @param initialContext
  * @param messages
  */
case class Controls(initialContext: RenderContext, messages: Seq[MessageExchanged]) {

  private var state       = MessageState(messages, 1)
  private var latestGraph = state.render(initialContext)
  private val minTime     = state.minTimeStamp.toInt
  private val maxTime     = state.maxTimeStamp.toInt

  private val timeSlider  = input(`type` := "range", min := minTime, max := maxTime, value := minTime, style := "width:100%").render
  private val speedSlider = input(`type` := "range", min := minTime, max := maxTime, value := minTime, style := "width:100%").render

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
    div(
      div(span("Time:"), timeSlider),
      div(span("Speed:"), speedSlider)
    ).render
  }
}
