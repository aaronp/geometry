package messaging

import cats.Id
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class MessageFrameTest extends WordSpec with Matchers {

  "MessageFrame.update" should {

    "make a new query for the current tick value when a current batch is not available" in {
      val api = apiFor(100 -> 200, 500 -> 600)

      val tickTime = 123

      val updatedFrame: MessageFrame[Id] = MessageFrame[cats.Id](0, 10.seconds, None, None).update(tickTime, api)
      updatedFrame.currentTimestampUTC shouldBe tickTime
    }
    "swap into the current batch the fetched batch when the tick time is not contained in that batch" in {
      ???
    }
    "make a query for the tick value when the tick time is not contained in the current or future batch" in {
      ???
    }
  }

  class TestApi(data: Seq[MessageRoundTrip]) extends MessageApi.Fixed(data) {
    val queries = ListBuffer[Range]()
    override def query(timeRange: Range) = {
      queries += timeRange
      super.query(timeRange)
    }
  }

  def apiFor(times: (Int, Int)*): TestApi = {
    val events = times.zipWithIndex.map {
      case ((from, to), i) => MessageRoundTrip(s"msg$i", "from" -> from.toLong, "to" -> to.toLong)
    }
    new TestApi(events)
  }
}
