package geometry

case class Circle(center: Point, radius: Double) {
  def intersects(other: Circle): Boolean = {
    distanceTo(other.center) <= radius + other.radius
  }

  def distanceTo(position: Point) = LineSegment(center, position).length

  def contains(position: Point): Boolean = distanceTo(position) <= radius
}
