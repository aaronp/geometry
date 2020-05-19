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

  def autoResize(vertScale: Double = 1.0) = {
    dom.window.onresize = e => {
      resize(h = (dom.window.outerHeight * vertScale).toInt)
    }
  }

  def resize(w: Int = dom.window.outerWidth, h: Int = dom.window.outerHeight) = {
    canvas.width = w
    canvas.height = h
  }

  def width  = canvas.width
  def height = canvas.height
  def withFont[A](font: String)(thunk: => A) = {
    val before = context.font
    context.font = font

    val a = thunk
    context.font = before
    a
  }

  def withTextAlign[A](align: String)(thunk: => A) = {
    val before = context.textAlign
    context.textAlign = align
    val a = thunk
    context.textAlign = before
    a
  }
  def withColor[A](color: String)(thunk: => A) = {
    val before = context.strokeStyle
    context.strokeStyle = color
    context.fillStyle = color
    context.beginPath()
    val result = thunk
    context.strokeStyle = before
    context.fillStyle = before
    result
  }

  def draw(text: String, at: Point, maxWidth: Option[Int] = None) = {
    val metrics = context.measureText(text)
    maxWidth match {
      case Some(w) =>
        context.fillText(s"$text, width: ${metrics.width}", at.x, at.y, w)
      case None =>
        context.fillText(s"$text, width: ${metrics.width}", at.x, at.y)
    }
  }

  def draw(arc: Arc) = {
    context.arc(arc.center.x, arc.center.y, arc.radius, arc.startRadian, arc.endRadian)
  }

  def draw(line: LineSegment) = {
    context.moveTo(line.x1, line.y1)
    context.lineTo(line.x2, line.y2)
//    context.stroke()
  }
  def draw(polygon: Polygon) = {
    polygon.points match {
      case head +: tail =>
        context.moveTo(head.x, head.y)
        tail.foreach {
          case Point(x, y) =>
            context.lineTo(x, y)
        }
        context.lineTo(head.x, head.y)
      case Seq() =>
    }
  }
  def draw(rectangle: Rectangle) = {
    import rectangle._
    context.strokeRect(x1, y2, rectangle.width, rectangle.height)
  }

  def bezierBetween(from: Rectangle, to: Rectangle) = {
    context.moveTo(from.x2, from.midY)
    val scale = 0.35
    val midX  = from.x2 + ((to.x1 - from.x2) * scale)
    val m2    = to.x1 - ((to.x1 - from.x2) * scale)
    context.bezierCurveTo(midX, from.midY, m2, to.midY, to.x1, to.midY)
  }

  def clear() = {
    context.clearRect(0, 0, width, height)
  }
  def fill = {
    context.fillRect(0, 0, width, height)
  }
}
