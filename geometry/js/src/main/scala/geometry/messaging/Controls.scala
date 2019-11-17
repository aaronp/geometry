package geometry.messaging

import geometry.Point
import scalatags.JsDom.all._

/**
  * A handle on the messaging controls
  */
class Controls(initialContext: RenderContext, api: MessageApi, startTime: Long, minTime: Int) {

  //api: MessageApi

//  def state: MessageState
//  private var latestGraph = state.render(initialContext)
//  private val maxTime     = state.maxTimeStamp.toInt
//
  private val initialMaxTime = (System.currentTimeMillis() - startTime).toInt
  private val timeSlider     = input(`type` := "range", min := minTime, max := initialMaxTime, value := minTime, style := "width:100%").render
  private val speedSlider    = input(`type` := "range", min := 0, max := 500, value := 100, style := "width:100%").render

  private def draw = {
    val pos = timeSlider.valueAsNumber
//    state = MessageState(messages, pos.toLong)
    //latestGraph = state.render(initialContext)
  }

  var latestGraph = Option.empty[InFlightRenderedGraph]
  timeSlider.onchange = _ => draw
  timeSlider.oninput = _ => draw

  initialContext.canvas.canvas.onmousemove = (event) => {
    latestGraph.foreach { current =>
      current.onMouseMove(Point(event.pageX, event.pageY))
    }
  }

  def render = {
    div(
      div(span("Time:"), timeSlider),
      div(span("Speed:"), speedSlider)
    ).render
  }
}
