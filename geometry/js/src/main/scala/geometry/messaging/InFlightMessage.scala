package geometry.messaging

import geometry.Point

final case class InFlightMessage(message: MessageExchanged, from: Point, to: Point, currentPosition: Point)
