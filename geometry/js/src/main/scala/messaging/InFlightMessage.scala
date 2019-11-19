package messaging

import geometry.Point

final case class InFlightMessage(message: MessageRoundTrip, from: Point, to: Point, currentPosition: Point)
