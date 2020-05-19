package geometry.game

import geometry.Point

sealed trait Weapon {
  def shoot(startPosition: Point, xVelocity: Double, yVelocity: Double, directionRads: Double): GameComponent
}

case class Gun(velocity: Int = 10) extends Weapon {
  override def shoot(startPosition: Point, xVelocity: Double, yVelocity: Double, directionRads: Double): GameComponent = {
    val dx = xVelocity + (Math.cos(directionRads) * velocity)
    val dy = yVelocity + (Math.sin(directionRads) * velocity)

    Bullet(startPosition, "red", directionRads = directionRads, xVelocity = dx, yVelocity = dy)
  }
}
