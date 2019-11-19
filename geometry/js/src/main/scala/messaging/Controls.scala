package messaging

import cats.kernel.Eq
import messaging.MessageFrame.Eval
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import monix.reactive.subjects.Var
import org.scalajs.dom.html.Div
import scalatags.JsDom.all._

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

    def emit() = {
      changes := currentPercentage()
    }

    sliderInput.onchange = _ => emit()
    sliderInput.oninput = _ => emit()

    def percentageFeed(debounceTimeout: FiniteDuration) = {
      changes.debounce(debounceTimeout).dump("pcnt")
    }
  }

  def percentageAsTime(minTime: Long, percent: Double): Long = {
    val now   = System.currentTimeMillis()
    val delta = (now - minTime) * percent
    minTime + delta.toLong
  }

  import scala.concurrent.duration._

  def apply[F[_]: Eval](api: MessageApi[F],
                        tickFrequency: FiniteDuration = 200.millis,
                        debounceTimeout: FiniteDuration = 500.millis,
                        batchQuerySize: FiniteDuration = 10.seconds) = {
    new Controls(api, tickFrequency, debounceTimeout, batchQuerySize, percentageAsTime(api.minEventTime, _))
  }

}

/**
  * A handle on the messaging controls
  */
class Controls[F[_]](api: MessageApi[F], //
                     tickFrequency: FiniteDuration, //
                     debounceTimeout: FiniteDuration, //
                     batchSize: FiniteDuration,        //
                     percentageAsTime: Double => Long) //
(implicit eval: MessageFrame.Eval[F]) //
{

  import Controls._

  def percentAsSpeed(pcnt: Double) = (pcnt * 2)

  private val timeSlider  = new Slider(1000, 1000, pcnt => s"${(pcnt * 100).toInt}%")
  private val speedSlider = new Slider(500, 1000, (percentAsSpeed _).andThen(speed => s"speed: ${speed}x "))

  private val currentTime = div().render

  private def updateCurrentTime(tick: TickState): Task[Unit] = {
    Task(currentTime.innerText = tick.instant.toString)
  }

  /**
    * A regular feed of timestamps which should be rendered, controlled by user controls for speed and the timestamp offset
    * @return
    */
  def timeOffsets: Observable[TickState] = {
    implicit val eq                   = Eq.fromUniversalEquals[Long]
    val timeOffsets: Observable[Long] = timeSlider.percentageFeed(debounceTimeout).map(percentageAsTime).distinctUntilChanged
    val observable = Observable.interval(tickFrequency).combineLatest(timeOffsets).scan(TickState(0, 0, 0)) {
      case (state, (_, offset)) =>
        if (state.lastSetOffset == offset) {
          state.advance(offset, tickFrequency, percentAsSpeed(speedSlider.currentPercentage()))
        } else {
          state.moveTo(offset, offset)
        }
    }

    observable.doOnNext(updateCurrentTime)
  }

  def batches: Observable[MessageFrame[F]] = {
    val initialFrame = MessageFrame[F](0, batchSize, None, None)
    timeOffsets.scan(initialFrame) {
      case (frame, tick) =>
        frame.update(tick.timeOffset, api)
    }
  }
  def messageFlow = batches.map(_.messagesForTimestamp)

  def render: Div = {
    div(
      div(span("Time:"), timeSlider.render),
      div(span("Speed:"), speedSlider.render),
      div(span("Current Time:"), currentTime.render)
    ).render
  }
}
