package geometry.game

import geometry.{Circle, Draw, Rectangle}

trait GameComponent {

  def checkCollision(element: Circle): Option[Velocity]

  def update(updated: GameState): Seq[GameComponent] = Nil

  def render(d: Draw): Unit
}
