package geometry.game
import geometry.{Circle, Draw, LineSegment, Point}

case class Explosion(center: Point, velocity: Velocity, time: Int = 1) extends GameComponent {
  override def render(d: Draw): Unit = {
    import d._
    withColor("green") {
      context.beginPath()

      val fromRads         = velocity.directionRads * 0.75
      val toRads           = velocity.directionRads * 1.25
      val shards           = 10
      val radDelta: Double = (toRads - fromRads) / shards

      (0 to shards).foreach { i =>
        val rads = fromRads + (i * radDelta)
        val cos  = Math.cos(rads) * velocity.speed
        val sin  = Math.sin(rads) * velocity.speed

        val dx1 = cos * time
        val dy1 = sin * time
        val dx2 = cos * (time + 1)
        val dy2 = sin * (time + 1)
        draw(LineSegment(center.x + dx1, center.y + dy1, center.x + dx2, center.y + dy2))
      }

      context.stroke()
    }
  }

  def incTime() = copy(time = time + 1)

  override def update(updated: GameState): Seq[GameComponent] = {
    if (time > 100) {
      Nil
    } else {
      incTime :: Nil
    }
  }

  override def checkCollision(element: Circle) = None
}
