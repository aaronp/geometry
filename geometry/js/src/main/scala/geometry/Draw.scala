package geometry

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas

object Draw {
  def apply(containerId: String) = {
    val canvas: Canvas = HtmlUtils.elmById(containerId) match {
      case c: Canvas => c
    }
    canvas.width = dom.window.outerWidth
    canvas.height = dom.window.outerHeight
    new Draw(canvas)
  }
}
case class Draw(canvas: Canvas) {

  // 2D. Get it?
  val context: CanvasRenderingContext2D = canvas.getContext("2d") match {
    case value: CanvasRenderingContext2D => value
  }

  def width  = canvas.width
  def height = canvas.height
  def withColor[A](color: String)(thunk: => A) = {
    val before = context.strokeStyle
    println(s"Using $color, before is $before")
    context.strokeStyle = color
    context.fillStyle = color
    val result = thunk
    context.strokeStyle = before
    context.fillStyle = before
    result
  }

  def draw(rectangle: Rectangle) = {
    import rectangle._
    context.moveTo(x1, y1)
    context.lineTo(x2, y1)
    context.lineTo(x2, y2)
    context.lineTo(x1, y2)
    context.lineTo(x1, y1)
    context.stroke()
  }

  def bezierBetween(from: Rectangle, to: Rectangle) = {
    context.moveTo(from.x2, from.midY)
    context.bezierCurveTo(to.x1, from.midY, from.x1, to.midY, to.x1, to.midY)
    context.stroke()
  }

  def clear() = {
    context.clearRect(0, 0, width, height)
  }
}
