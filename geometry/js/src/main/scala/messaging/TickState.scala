package messaging

import java.time.Instant

import scala.concurrent.duration.FiniteDuration

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
