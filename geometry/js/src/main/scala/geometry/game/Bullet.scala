package geometry.game

import geometry._

case class Bullet(position: Point, color: String, directionRads: Double = 0.0, xVelocity: Double = 0.0, yVelocity: Double = 0.0, size: Int = 30) extends GameComponent {

  override def checkCollision(element: Circle) = {
    if (element.contains(position)) {
      val speed = LineSegment(0, 0, xVelocity, yVelocity).length
      Option(Velocity(directionRads, speed))
    } else {
      None
    }
  }

  override def update(updated: GameState): Seq[GameComponent] = {
    val newPos = Point(position.x + xVelocity, position.y + yVelocity)
    if (updated.bounds.contains(newPos)) {
      copy(position = newPos) :: Nil
    } else {
      Nil
    }
  }

  def scaleVelocity(xDelta: Double = 0.0, yDelta: Double = 0.0) = {
    copy(xVelocity = xVelocity * xDelta, yVelocity = yVelocity * yDelta)
  }
  def changeVelocity(xDelta: Double = 0.0, yDelta: Double = 0.0) = {
    copy(xVelocity = xVelocity + xDelta, yVelocity = yVelocity + yDelta)
  }
  override def render(d: Draw) = {
    import d._
    withColor(color) {
      context.beginPath()

      val dx = Math.cos(directionRads) * 10
      val dy = Math.sin(directionRads) * 10

      draw(Polygon(position, position.translate(dx, dy)))
      context.stroke()
    }
  }
}
