package geometry

import geometry.Plot.Layout.AsciiLine

case class LineSegment(from: Point, to: Point) {
  override def toString = {
    s"$from to $to ($slopeInterceptFormula)"
  }

  def reverse = LineSegment(to, from)

  def x1 = from.x
  def y1 = from.y

  def x2 = to.x
  def y2 = to.y

  def width  = to.x - from.x
  def height = to.y - from.y

  def midpoint = Point(from.x + (width / 2), from.y + (height / 2))

  def scaledPoint(percent: Double): Point = {
    if (slope.isVertical || slope.value.abs >= 1.0) {
      // choose a point on the y axis
      val yPoint = y1 + ((y2 - y1) * percent)
      Point(xValueAt(yPoint), yPoint)
    } else {
      // choose a point on the x axis
      val xPoint = x1 + ((x2 - x1) * percent)
      Point(xPoint, yValueAt(xPoint))
    }
  }

  def length: Double = {
    val xDelta = x2 - x1
    val yDelta = y2 - y1
    val xSqrd  = xDelta * xDelta
    val ySqrd  = yDelta * yDelta
    Math.sqrt(xSqrd + ySqrd)
  }

  /** @param y the y coordinate
    * @return
    */
  def xValueAt(y: Double) = {
    if (slope.isVertical) {
      x1
    } else if (m.isNaN) {
      m
    } else {
      (y - b) / m
    }
  }

  def yValueAt(x: Double) = {
    if (slope.isHorizontal) {
      y1
    } else if (m.isNaN) {
      m
    } else {
      (x * m) + b
    }
  }

  def ascii(char: Char = '.', box: Rectangle = boundingBox): Plot.AsciiRows = new AsciiLine(char).layout(this, box)

  def clip(bounds: Rectangle): Option[LineSegment] = {
    clipPoints(bounds) match {
      case List(a, b) => Option(LineSegment(a, b))
      case _          => None
    }
  }

  /**
    * Todo - implement via Sutherland-Hodgman (https://en.wikipedia.org/wiki/Sutherland%E2%80%93Hodgman_algorithm)
    *
    * @param bounds the bounding rectangle
    * @return the clipped points
    */
  def clipPoints(bounds: Rectangle): List[Point] = {
    val edge = this
    if (bounds.contains(edge.from)) {
      if (bounds.contains(edge.to)) {
        List(edge.from, edge.to)
      } else {
        // the 'from' point is in the bounds but 'to' is not -- where's the intersection?
        val intersectionPoint = bounds.edges.view.flatMap { boundingEdge =>
          boundingEdge.intersect(edge)
        }
        edge.from :: intersectionPoint.toList
      }
    } else if (bounds.contains(edge.to)) {
      val intersectionPoint = bounds.edges.view.flatMap { boundingEdge =>
        boundingEdge.intersect(edge)
      }
      edge.to :: intersectionPoint.toList
    } else {
      // the complicated case -- neither point is in the box, but is there an intersection w/ any of the edges?
      def clipTop(point: Point)    = if (edge.to.y > edge.from.y) List(edge.from, point) else List(point, edge.to)
      def clipRight(point: Point)  = if (edge.to.x > edge.from.x) List(edge.from, point) else List(point, edge.to)
      def clipBottom(point: Point) = if (edge.to.y < edge.from.y) List(edge.from, point) else List(point, edge.to)
      def clipLeft(point: Point)   = if (edge.to.x > edge.from.x) List(edge.from, point) else List(point, edge.to)
      val opt: Option[List[Point]] = bounds.topEdge
        .intersectPoint(edge)
        .map(clipTop)
        .orElse(bounds.rightEdge.intersectPoint(edge).map(clipRight))
        .orElse(bounds.bottomEdge.intersectPoint(edge).map(clipBottom))
        .orElse(bounds.leftEdge.intersectPoint(edge).map(clipLeft))

      opt.getOrElse(Nil)
    }
  }

  def slopeInterceptFormula = s"y = ${slope}x + $b"

  def slopeInDegrees: Double = radToDeg(slopeInRadians)

  def arrowTip(len: Double, spreadInDegrees: Double = 90.0): (LineSegment, LineSegment) = {
    val halfSpread = spreadInDegrees / 2
    val lineSlope  = slopeInDegrees
    def mkArrowLeg(degrees: Double) = {
      val rad     = degToRad(lineSlope + degrees)
      val xOffset = Math.cos(rad) * len
      val yOffset = Math.sin(rad) * len
      LineSegment(Point(to.x + xOffset, to.y + yOffset), to)
    }
    val arrow1 = mkArrowLeg(180 + halfSpread)
    val arrow2 = mkArrowLeg(180 - halfSpread)
    (arrow1, arrow2)
  }

//  def arrowPoints(len: Double, spreadInDegrees: Double = 90.0) = {
//    val (a,b) = arrowTip(len, spreadInDegrees)
//    (a.from, b.from, to)
//  }

  def slopeInRadians: Double = {
    // vertical?
    if (x2 == x1) {
      if (y2 > y1) Math.PI / 2 else 1.5 * Math.PI
      // horizontal?
    } else if (y2 == y1) {
      if (x2 > x1) 0.0 else Math.PI
    } else {
      // some degree...
      if (x2 > x1) {
        if (y2 > y1) {
          Math.atan(slope.value)
        } else {
          (2 * Math.PI) + Math.atan(slope.value)
        }
      } else {
        require(x2 < x1)
        Math.PI + Math.atan(slope.value)
      }
    }
  }

  def slope: Slope = {
    val rise = to.y - from.y
    val run  = to.x - from.x
    if (run == 0.0) {
      Slope(Double.PositiveInfinity)
    } else {
      Slope(rise / run)
    }
  }

  // line formula: y = mx + b
  lazy val b = from.y - (m * from.x)

  def m: Double = slope.value

  /** THE RETURN POINT MAY NOT BE ON THE LINE.
    *
    * The returned point will only be NONE if the two lines are parallel. To get the actual intersection on the line, use [[intersect]]
    *
    * @param other the other line segment (which is treated as a ray)
    * @return the point at which these two lines would intersect if they were infinite rays
    */
  def intersectPoint(other: LineSegment): Option[Point] = {
    val rhs = other.b - b
    if (other.slope.isVertical) {
      if (slope.isVertical) {
        // we could check if it's the same x coord, but that would just get us infinite intersections
        None
      } else {
        // to intersect a vertical line with a non-vertical line, we just plug-in the vertical line's x coord to our formula

        val x = other.from.x.ensuring(_ == other.to.x)
        val y = (m * x) + b
        Option(Point(x, y))
      }
    } else if (slope.isVertical) {
      other.intersectPoint(this)
    } else {
      val multiplier: Slope = (slope - other.slope).reciprocal
      val isParallel        = multiplier.isVertical
      if (isParallel) {
        None
      } else {
        val xResult = multiplier.value * rhs
        val yResult = m * xResult + b
        Option(Point(xResult, yResult))
      }
    }
  }
  lazy val boundingBox = Rectangle(from, to)

  def intersects(other: LineSegment): Boolean = intersect(other).nonEmpty

  def intersect(other: LineSegment): Option[Point] = {
    intersectPoint(other).filter(p => boundingBox.contains(p) && other.boundingBox.contains(p))
  }
}

object LineSegment {
  def apply(x1: Double, y1: Double, x2: Double, y2: Double): LineSegment = {
    new LineSegment(Point(x1, y1), Point(x2, y2))
  }
}
