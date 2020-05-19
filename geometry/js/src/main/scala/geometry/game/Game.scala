package geometry.game

import geometry.{Draw, Point}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import monix.reactive.subjects.ConcurrentSubject
import org.scalajs.dom

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
@JSExportTopLevel("Game")
object Game {

  @JSExport
  def render(controlsId: String, containerId: String): Unit = {
    val draw = Draw(containerId)
    draw.autoResize(0.8)

    gameLoop(GameState(draw))
  }

  def testExplosion(draw: Draw) = {
    var expl = Explosion(Point(600, 600), Velocity(3, 8))
    dom.window.onkeydown = e => {
      println(s"key=${e.key}, code=${e.keyCode}")
      expl = expl.incTime()
      draw.clear()
      expl.render(draw)
    }
    expl.render(draw)
  }

  def gameLoop(initialState: GameState) = {
    val eventSubject: ConcurrentSubject[Event, Event] = ConcurrentSubject.publish[Event]

    // send keyboard events to our subject
    publishMouseEventsTo(eventSubject)

    // send redraw events every so often
    Observable.interval(initialState.redrawFreq).map(_ => Redraw).subscribe(eventSubject)

    // scan our state over the events
    eventSubject.scan(initialState)(_ update _).foreach(_ => ())
  }

  def publishMouseEventsTo(eventSubject: ConcurrentSubject[Event, Event]) = {
    dom.window.onkeydown = e => {
      KeyMap.eventForKey(e.keyCode).foreach { event =>
        e.stopPropagation()
        e.preventDefault()
        eventSubject.onNext(event)
      }
    }
    dom.window.onkeyup = e => {
      KeyMap.eventForKey(e.keyCode).foreach { _ =>
        e.stopPropagation()
        e.preventDefault()
      }
    }
    dom.window.onkeypress = e => {
      KeyMap.eventForKey(e.keyCode).foreach { _ =>
        e.stopPropagation()
        e.preventDefault()
      }
    }
  }
  //    var rads = 0.0
  //val radBx = input(`type` := "text", id := "rs", name := "Radians")("Radians").render
  //    val c     = HtmlUtils.elmById(controlsId)
  //    c.appendChild(radBx)

  //    radBx.onkeydown = e => {
  //      try {
  //        rads = radBx.value.toDouble
  //        println(s"${rads} radians")
  //      } catch {
  //        case _ =>
  //      }
  //    }

  //
  //    draw.canvas.onkeyup = e => {
  //      println("Canvas : " + e.keyCode)
  //    }

}
