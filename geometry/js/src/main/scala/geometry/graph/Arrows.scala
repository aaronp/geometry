package geometry.graph

import geometry._
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
@JSExportTopLevel("Arrows")
object Arrows {

  def lines(center: Point, len: Double) = {
    (0 to 345 by 15).map { deg =>
      val radians = degToRad(deg)
      val x2      = Math.cos(radians) * len
      val y2      = Math.sin(radians) * len
      LineSegment(center, Point(center.x + x2, center.y + y2))
    }
  }
  @JSExport
  def render(containerId: String) = {
    val canvas: Canvas = HtmlUtils.elmById(containerId) match {
      case c: Canvas => c
    }

    // 2D. Get it?
    val dd: CanvasRenderingContext2D = canvas.getContext("2d") match {
      case value: CanvasRenderingContext2D => value
    }

    canvas.width = dom.window.outerWidth
    canvas.height = dom.window.outerHeight
    val d = Draw(canvas)
    lines(Point(300, 300), 240).foreach { line =>
      d.setColor("green")
      d.draw(line)

      val (a, b) = line.arrowTip(30, spreadInDegrees = 60)
      d.draw(a)
      d.draw(b)
      dd.stroke()
      dd.closePath()

      d.setColor("red")
      dd.beginPath()
      d.draw(f"${line.to} : ${line.slopeInDegrees}%1.2f or ${line.slope}", line.to)
      dd.closePath()
    }
  }

}
