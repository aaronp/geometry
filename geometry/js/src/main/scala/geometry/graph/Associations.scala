package geometry.graph

import geometry.{HtmlUtils, Rectangle}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
@JSExportTopLevel("Associations")
object Associations {

  case class Elem(id: String, label: String, box: Rectangle)
  case class Graph(nodes: Seq[Elem], associations: Set[(String, String)])

  @JSExport
  def render(controlsDivId: String, containerId: String) = {
    val canvas: Canvas = HtmlUtils.elmById(containerId) match {
      case c: Canvas => c
    }

    // 2D. Get it?
    val dd: CanvasRenderingContext2D = canvas.getContext("2d") match {
      case value: CanvasRenderingContext2D => value
    }

    canvas.width = dom.window.outerWidth
    canvas.height = dom.window.outerHeight
  }

  case class Container(dd: CanvasRenderingContext2D, graph: Graph) {}
}
