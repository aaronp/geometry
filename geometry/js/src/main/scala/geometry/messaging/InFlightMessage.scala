package geometry.messaging

import geometry.Point

final case class InFlightMessage(message: Message, from: Point, to: Point, currentPosition: Point)
