package geometry.game

sealed trait Event
case object Redraw                                 extends Event
case class UpdatePlayer(cmd: Command, player: Int) extends Event
