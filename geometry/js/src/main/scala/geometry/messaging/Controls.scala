package geometry.messaging

import geometry.HtmlUtils
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import monix.reactive.subjects.Var
import org.scalajs.dom.html.Div
import scalatags.JsDom.all._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object Controls {

  /**
    *  A slider control
    * @param initialValue
    * @param maxValue
    */
  class Slider(initialValue: Int, maxValue: Int = 1000, calcValue: Double => String) {
    private val changes     = Var[Double](initialValue / maxValue.toDouble)
    private val sliderInput = input(`type` := "range", min := 0, max := maxValue, value := initialValue, style := "width:100%").render

    private val displayDiv = span(style := "width:100%").render
    def render = {
      currentPercentage()
      div(sliderInput, displayDiv)
    }
    def currentPercentage(): Double = {
      val pcnt = sliderInput.valueAsNumber / maxValue.toDouble

      displayDiv.innerText = calcValue(pcnt)

      pcnt
    }

    sliderInput.onchange = _ => {
      changes := currentPercentage()
    }
    sliderInput.oninput = _ => {
      changes := currentPercentage()
    }

    def percentageFeed(debounceTimeout: FiniteDuration) = {
      changes.debounce(debounceTimeout).dump("pcnt")
    }
  }

  def percentageAsTime(minTime: Long, now: Long, percent: Double): Long = {
    val millis = (now - minTime) * percent
    millis.toLong
  }

  import scala.concurrent.duration._

  def apply(api: MessageApi, debounceTimeout: FiniteDuration = 500.millis): Controls = {
    new Controls(api, debounceTimeout, percentageAsTime(api.minEventTime, System.currentTimeMillis(), _))
  }
}

/**
  * A handle on the messaging controls
  */
class Controls(api: MessageApi, debounceTimeout: FiniteDuration, percentageAsTime: Double => Long) {

  import Controls._

  def percentAsSpeed(pcnt: Double) = {
    (pcnt * 200).toInt
  }

  private val timeSlider  = new Slider(1000, 1000, percentageAsTime.andThen(time => s"${time}ms"))
  private val speedSlider = new Slider(500, 1000, (percentAsSpeed _).andThen(speed => s"speed: $speed/100 "))

  def messageFlow: Observable[Message] = {
    val messages: Observable[Message] = timeSlider.percentageFeed(debounceTimeout).flatMap { timePercent =>
      val epoch = percentageAsTime(timePercent)
      api.from(epoch)
    }

    //messages.bufferSliding(2, 1)

    def waitPercent(pcnt: Double) = {
      Task.fromFuture(Delay(1, ()))
    }
    speedSlider.percentageFeed(debounceTimeout).mapEval(waitPercent)

    val throttle = {
      val taskTask: Task[Task[Double]] = Task {
        val pcnt = speedSlider.currentPercentage()

        val waitFor = if (pcnt > 0) {
          val millis = (2000 * pcnt).toLong
          2000 - millis
        } else {
          200
        }
        HtmlUtils.log(s"Throttling $pcnt% ($waitFor ms)")
        val future = Delay(waitFor, pcnt)
        Task.fromFuture(future)
      }
      taskTask.flatten.restartUntil(_ > 0)
    }

    messages.mapEval { a =>
      HtmlUtils.log(s"\tonNext($a)")
      throttle.map(_ => a)
    }

    messages.bufferWithSelector()
  }

  def render: Div = {
    div(
      div(span("Time:"), timeSlider.render),
      div(span("Speed:"), speedSlider.render)
    ).render
  }
}
