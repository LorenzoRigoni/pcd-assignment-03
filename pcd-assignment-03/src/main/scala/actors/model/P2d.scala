package actors.model

final case class P2d(x: Double, y: Double):
  def sum(v: V2d): P2d = P2d(x + v.x, y + v.y)

  def sub(p: P2d): V2d = V2d(x - p.x, y - p.y)

  def distance(p: P2d): Double =
    val dx = p.x - x
    val dy = p.y - y
    math.sqrt(dx * dx + dy * dy)

  override def toString: String = s"P2d($x, $y)"