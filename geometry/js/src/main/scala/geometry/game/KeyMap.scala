package geometry.game
import cats.syntax.option._

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
    code match {
      case v if v.up                     => UpdatePlayer(Up, 1).some
      case v if v.down                   => UpdatePlayer(Down, 1).some
      case v if v.left                   => UpdatePlayer(Left, 1).some
      case v if v.right                  => UpdatePlayer(Right, 1).some
      case v if v.rotateClockwise        => UpdatePlayer(RotateClockwise, 1).some
      case v if v.rotateCounterClockwise => UpdatePlayer(RotateCounterClockwise, 1).some
      case v if v.space                  => UpdatePlayer(Shoot, 1).some
      case _ =>
        println("other " + code)
        None
    }
  }
}
