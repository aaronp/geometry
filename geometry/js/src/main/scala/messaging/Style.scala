package messaging

final case class Style(circleRadius: Double, nodeRadius: Double, messageNodeRadius: Double) {
  def colorFor(nodeIndex: Int, totalIndices: Int): String = {
    val of = List("#6b5b95", "#feb236", "#d64161", "#ff7b25", "#a2b9bc", "#b2ad7f", "#878f99", "#6b5b95")
    of(nodeIndex % of.size)
  }
}
