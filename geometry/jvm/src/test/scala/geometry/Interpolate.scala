package geometry

import scala.collection.immutable

object Interpolate {

  def pointsOnCircle(center: Point, radius: Double, points: Int): immutable.IndexedSeq[Point] = {
    val radians = 2 * Math.PI
    Interpolate(0, radians, points).map { r =>
      val x = Math.cos(r) * radius
      val y = Math.sin(r) * radius
      Point(center.x + x, center.y + y)
    }
  }

  def apply(from: Double, to: Double, steps: Int): immutable.IndexedSeq[Double] = {
    val diff          = to - from
    val delta: Double = diff / steps
    (0 until steps).map { i =>
      from + (delta * i)
    }
  }
}
