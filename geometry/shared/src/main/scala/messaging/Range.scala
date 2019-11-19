package messaging

final case class Range(from: Long, to: Long) {
  def contains(x: Long): Boolean = !(x < from || x > to)
}
