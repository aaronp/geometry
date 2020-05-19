package geometry.game

import geometry.{Arc, Circle, Draw, Point}

case class Ship(position: Point, color: String, weapon: Weapon = Gun(), directionRads: Double = 0.0, xVelocity: Double = 0.0, yVelocity: Double = 0.0, size: Int = 30)
    extends GameComponent {

  override def checkCollision(element: Circle) = {
    if (element.intersects(boundingCircle)) {
      Option(Velocity(xVelocity, yVelocity))
    } else {
      None
    }
  }

  lazy val boundingCircle = Circle(position, size)

  def shoot(): GameComponent = {
    weapon.shoot(position, xVelocity, yVelocity, directionRads)
  }
  override def update(state: GameState): Seq[GameComponent] = {
    updateShip(state) :: Nil
  }

  def updateShip(state: GameState): Ship = {
    val newX = position.x + xVelocity match {
      case x if x < 0                => state.draw.width
      case x if x > state.draw.width => 0
      case x                         => x
    }

    val newY = position.y + yVelocity match {
      case y if y < 0                 => state.draw.height
      case y if y > state.draw.height => 0
      case y                          => y
    }

    val newPos = Point(newX, newY)
    copy(position = newPos)
  }

  def rotate(delta: Double): Ship = {
    copy(directionRads = directionRads + delta)
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
      val from = directionRads + 0.4
      val to   = directionRads - 0.4
      draw(Arc(position, size, from, to))
      context.stroke()
    }
  }

}

object Ship {
  def playerOne() = Ship(Point(100, 100), "blue")
}
