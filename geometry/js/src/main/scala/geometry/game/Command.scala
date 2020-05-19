package geometry.game

sealed trait Command
case object Up                     extends Command
case object Down                   extends Command
case object Left                   extends Command
case object Right                  extends Command
case object RotateClockwise        extends Command
case object RotateCounterClockwise extends Command
case object Shoot                  extends Command
