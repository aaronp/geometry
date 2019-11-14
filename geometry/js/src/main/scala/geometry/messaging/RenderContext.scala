package geometry.messaging

import geometry.Point
import org.scalajs.dom.CanvasRenderingContext2D

final case class RenderContext(canvas: CanvasRenderingContext2D, style: Style, width: Int, height: Double) {
  def center: Point = Point(width / 2, height / 2)
}
