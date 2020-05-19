package geometry.game

import geometry.{Arc, Circle, Draw, Point}

case class Ship(position: Point, color: String, weapon: Weapon = Gun(), directionRads: Double = 0.0, xVelocity: Double = 0.0, yVelocity: Double = 0.0, radius: Int = 30)
    extends GameComponent {

  override def checkCollision(element: Circle) = {
    if (element.intersects(boundingCircle)) {
      Option(Velocity(xVelocity, yVelocity))
    } else {
      None
    }
  }
  val offsetX = position.x - radius
  val offsetY = position.y - radius

  lazy val boundingCircle = Circle(position, radius)

  def shoot(): GameComponent = {

    val gunX = position.x + (Math.cos(directionRads) * radius)
    val gunY = position.y + (Math.sin(directionRads) * radius)
    weapon.shoot(Point(gunX, gunY), xVelocity, yVelocity, directionRads)
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
  def render(d: Draw, assets: Assets) = {

    // the png is facing upwards, not to the right (e.g. 0rad)
    val shipRads = directionRads // directionRads + (0.5 * Math.PI)
    import d._
    context.translate(position.x, position.y)
    context.rotate(shipRads)
    context.translate(-position.x, -position.y)
    context.drawImage(assets.ship1, offsetX, offsetY, radius * 2, radius * 2)

    // reset
    context.setTransform(1, 0, 0, 1, 0, 0)
  }

  override def render(d: Draw) = {
    import d._
    withColor(color) {
      context.beginPath()
      val from = directionRads + 0.4
      val to   = directionRads - 0.4
      draw(Arc(position, radius, from, to))
      context.stroke()
    }
  }

}

object Ship {
  def playerOne() = Ship(Point(100, 100), "blue")
}
