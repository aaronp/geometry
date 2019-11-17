package geometry.messaging

import monix.reactive.Observable

trait MessageApi {
  def from(epochMillisUTC : Long) : Observable[Message]
}

object MessageApi {
  def test() : MessageApi = {
    TestM
  }
}