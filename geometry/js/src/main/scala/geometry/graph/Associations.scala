package geometry.graph

import geometry.{Draw, Point, Rectangle}
import org.scalajs.dom.MouseEvent

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

//  class Elm(override val toString: String, originalUpdate: Draw => Unit) {
//    private var update = originalUpdate
//    def update(newUpdate: Draw => Unit) = {
//      update = newUpdate
//    }
//    def draw(d: Draw) = update.apply(d)
//    override def equals(obj: Any): Boolean = {
//      obj match {
//        case e: Elm => toString == e.toString
//        case _      => false
//      }
//    }
//    override def hashCode(): Int = toString.hashCode
//  }
//  object Elm {
//    def apply(f: Draw => Unit) = new Elm(UUID.randomUUID().toString, f)
//  }

  @JSExport
  def render(controlsDivId: String, containerId: String) = {

    var rectangle1 = Rectangle(300, 20, 140, 250)
    val rectangle2 = Rectangle(600, 200, 680, 650)

    var dragged = Option.empty[(Point, Rectangle)]

    def debug(e: MouseEvent) = {
      import e._

      println(s"""screen:${screenX} X ${screenY}
           |client:${clientX} x ${clientY}
           |page:${pageX} x ${pageY}
           |""".stripMargin)
    }

    val draw = Draw(containerId)

    def redraw() = {
      draw.clear()
      draw.withColor("red") {
        draw.context.beginPath()
        draw.draw(rectangle1)
        draw.draw(rectangle2)
        draw.bezierBetween(rectangle1, rectangle2)
        draw.context.stroke()
      }
    }

    draw.canvas.onmousedown = e => {
      val point = Point(e.clientX, e.clientY)
      dragged = Option(rectangle1).filter(_.contains(point)).map(point -> _)
      debug(e)
    }

    draw.canvas.onmousemove = e => {
      dragged.foreach {
        case (Point(x, y), originalShape) =>
          val deltaX = e.clientX - x
          val deltaY = e.clientY - y

          rectangle1 = originalShape.translate(deltaX, deltaY)

          redraw()
      }
    }

    draw.canvas.onmouseup = e => {
      dragged = None
    }

    redraw()
  }
}
