package geometry.game

import geometry.{Draw, Rectangle}

import scala.concurrent.duration._

case class GameState(draw: Draw,
                     p1: Ship = Ship.playerOne(),
                     components: List[GameComponent] = Nil,
                     collision: Boolean = false,
                     speedInc: Double = 2.0,
                     turnInc: Double = 0.2,
                     redrawFreq: FiniteDuration = 60.millis,
                     frictionMultiplier: Double = 1.0) {

  lazy val bounds = Rectangle(0, 0, draw.width, draw.height)
  def update(event: Event): GameState = {
    event match {
      case Redraw =>
        val p1Updated         = p1.updateShip(this)
        val newComponents     = components.flatMap(_.update(this))
        val collisionVelocity = newComponents.view.flatMap(_.checkCollision(p1Updated.boundingCircle)).headOption
        val explosions: Seq[GameComponent] = collisionVelocity.fold(List[GameComponent]()) { v =>
          Explosion(p1Updated.position, v) :: Nil
        }
        val updated = copy(p1 = p1Updated, components = explosions ++: newComponents, collision = collisionVelocity.isDefined)
        updated.redraw()
        updated

      case UpdatePlayer(cmd, _) =>
        onCommand(cmd)
    }
  }

  override def toString: String = s"STATE: $p1"
  def redraw() = {
    draw.clear()
    render(draw)
  }

  def onCommand(cmd: Command): GameState = {
    cmd match {
      case Up =>
        copy(p1 = p1.changeVelocity(yDelta = -speedInc))
      case Down =>
        copy(p1 = p1.changeVelocity(yDelta = speedInc))
      case Left =>
        copy(p1 = p1.changeVelocity(xDelta = -speedInc))
      case Right =>
        copy(p1 = p1.changeVelocity(xDelta = speedInc))
      case RotateClockwise =>
        copy(p1 = p1.rotate(-turnInc))
      case RotateCounterClockwise =>
        copy(p1 = p1.rotate(turnInc))
      case Shoot =>
        val bullet = p1.shoot()
        println("pow! " + bullet)
        copy(components = bullet :: components)
    }
  }

  def render(d: Draw) = {
    components.foreach(_.render(d))
    p1.render(d)
  }
}
