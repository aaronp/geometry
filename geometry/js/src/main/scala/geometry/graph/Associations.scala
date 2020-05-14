package geometry.graph

import geometry.{Draw, Rectangle}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
@JSExportTopLevel("Associations")
object Associations {

  case class Elem(id: String, label: String, box: Rectangle)
  case class Graph(nodes: Seq[Elem], associations: Set[(String, String)])

  def testData = {
    val e1 = Elem("1", "First", Rectangle(10, 10, 120, 250))
    val e2 = Elem("2", "Second", Rectangle(300, 20, 140, 250))
    Graph(Seq(e1, e2), Set("1" -> "2"))
  }
  @JSExport
  def render(controlsDivId: String, containerId: String) = {
    val draw = Draw(containerId)

    val r1 = Rectangle(300, 20, 140, 250)
    val r2 = Rectangle(600, 200, 680, 650)
    draw.withColor("#FF0000") {
      draw.draw(r1)
    }
    draw.withColor("#0000FF") {
      draw.draw(r2)
    }
    draw.withColor("#00FF0") {
      draw.bezierBetween(r1, r2)
    }

  }

}
