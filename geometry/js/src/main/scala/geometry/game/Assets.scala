package geometry.game

import geometry.HtmlUtils
import org.scalajs.dom.html.Audio
import org.scalajs.dom.raw.HTMLElement

case class Assets(
    ship1: HTMLElement,
    bangAudio: Audio,
    pewAudio: Audio
) {

  def bang() = {
    bangAudio.pause()
    bangAudio.play()
  }
  def pew() = {
    pewAudio.pause()
    pewAudio.play()
  }
}
object Assets extends HtmlUtils {

  def apply(bangId: String = "sound-bang", pewId: String = "sound-pew", ship1Img: String = "ship1") = {

    new Assets(elmById(ship1Img).asInstanceOf[HTMLElement], audioById(bangId), audioById(pewId))
  }
}
