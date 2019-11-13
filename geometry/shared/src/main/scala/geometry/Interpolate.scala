package geometry

object Interpolate {
  def pointsOnCircle(center: Point, radius: Double, points: Int): Seq[Point] = {
    val radians = 2 * Math.PI
    Interpolate(0, radians, points).map { r =>
      val x = Math.cos(r) * radius
      val y = Math.sin(r) * radius
      Point(center.x + x, center.y + y)
    }
  }

  def apply(from: Double, to: Double, steps: Int): Seq[Double] = {
    val diff          = to - from
    val delta: Double = diff / steps
    (0 until steps).map { i =>
      from + (delta * i)
    }
  }
}
