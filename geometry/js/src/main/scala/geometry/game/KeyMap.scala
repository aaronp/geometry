package geometry.game
import cats.syntax.option._
import org.scalajs.dom.KeyboardEvent

object KeyMap {

  implicit class RichKeyCode(val code: Int) extends AnyVal {

    def isA = code == 65
    def isS = code == 83
    def isD = code == 68
    def isW = code == 87

    def isJ = code == 74
    def isK = code == 75
    def isL = code == 76
    def isI = code == 73

    def rotateClockwise        = isA
    def rotateCounterClockwise = isD

    def up    = isI || upArrow
    def left  = isJ || leftArrow
    def right = isL || rightArrow
    def down  = isK || downArrow

    def upArrow    = code == 38
    def leftArrow  = code == 37
    def rightArrow = code == 39
    def downArrow  = code == 40

    def space = code == 32
  }

  def eventForKey(code: Int) = {
    ???
  }
  def eventFor(keyEvt: KeyboardEvent) = {
    keyEvt.key match {
      case "w" => UpdatePlayer(Up, 1).some
      case "s" => UpdatePlayer(Down, 1).some
      case "a" => UpdatePlayer(Left, 1).some
      case "d" => UpdatePlayer(Right, 1).some
      case "e" => UpdatePlayer(RotateClockwise, 1).some
      case "q" => UpdatePlayer(RotateCounterClockwise, 1).some
      case "f" => UpdatePlayer(Shoot, 1).some
      case _   => None
    }
  }
}
