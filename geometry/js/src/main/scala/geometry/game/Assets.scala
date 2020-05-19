package geometry.game

import geometry.HtmlUtils
import org.scalajs.dom.html.Audio
import org.scalajs.dom.raw.HTMLElement

case class Assets(
    ship1: HTMLElement,
    bangAudio: Audio
) {

  def bang() = bangAudio.play()
}
object Assets extends HtmlUtils {

  def apply(bangId: String) = {

    new Assets(elmById("ship1").asInstanceOf[HTMLElement], audioById(bangId))
  }
}
