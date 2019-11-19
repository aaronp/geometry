package messaging

import java.time.Instant

import cats.Id
import cats.kernel.Eq
import geometry.HtmlUtils
import messaging.MessageFrame.Eval
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

  def apply[F[_] : Eval](api: MessageApi[F], tickFrequency: FiniteDuration = 200.millis, debounceTimeout: FiniteDuration = 500.millis): Controls = {
    new Controls(api, tickFrequency, debounceTimeout, percentageAsTime(api.minEventTime, _))
  }

  case class TickState(lastSetOffset: Long, previousTimeOffset: Long, timeOffset: Long) {
    def previousInstant = Instant.ofEpochMilli(previousTimeOffset)
    def instant         = Instant.ofEpochMilli(timeOffset)
//    override def toString: String = s"{prev:$previousInstant, time:$instant}"
    def asRange(tickFrequency: FiniteDuration) =
      if (previousTimeOffset <= timeOffset) {
        Range(previousTimeOffset, timeOffset)
      } else {
        Range(timeOffset, timeOffset + tickFrequency.toMillis)
      }
    def moveTo(newLastSetOffset: Long, newTime: Long): TickState = {
      copy(lastSetOffset = newLastSetOffset, previousTimeOffset = timeOffset, timeOffset = newTime)
    }

    def advance(newLastSetOffset: Long, tickFrequency: FiniteDuration, speedPercentage: Double): TickState = {
      val newTime = timeOffset + ((tickFrequency.toMillis * speedPercentage).toLong)
      moveTo(newLastSetOffset, newTime)
    }
    def isIncreasing = timeOffset >= previousTimeOffset
  }
}

/**
  * A handle on the messaging controls
  */
class Controls(api: MessageApi[cats.Id], tickFrequency: FiniteDuration, debounceTimeout: FiniteDuration, batchQueryRange: FiniteDuration, percentageAsTime: Double => Long) {

  import Controls._

  def percentAsSpeed(pcnt: Double) = (pcnt * 2)

  private val timeSlider  = new Slider(1000, 1000, pcnt => s"${(pcnt * 100).toInt}%")
  private val speedSlider = new Slider(500, 1000, (percentAsSpeed _).andThen(speed => s"speed: ${speed}x "))

  def timeOffsets: Observable[TickState] = {
    implicit val eq                   = Eq.fromUniversalEquals[Long]
    val timeOffsets: Observable[Long] = timeSlider.percentageFeed(debounceTimeout).map(percentageAsTime).distinctUntilChanged
    Observable.interval(tickFrequency).combineLatest(timeOffsets).scan(TickState(0, 0, 0)) {
      case (state, (_, offset)) =>
        if (state.lastSetOffset == offset) {
          state.advance(offset, tickFrequency, percentAsSpeed(speedSlider.currentPercentage()))
        } else {
          state.moveTo(offset, offset)
        }
    }
  }

  def batches = {

    timeOffsets.scan()
    ???
  }
  def messageFlow: Observable[MessageRoundTrip] = {
    // a stream which ends once the user
    timeOffsets.flatMap { tick =>
      api.query(tick.asRange(tickFrequency))
    }
  }

  def render: Div = {
    div(
      div(span("Time:"), timeSlider.render),
      div(span("Speed:"), speedSlider.render)
    ).render
  }
}
