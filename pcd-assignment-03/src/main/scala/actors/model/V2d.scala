package actors.model

final case class V2d(x: Double, y: Double):
  def sum(v: V2d): V2d = V2d(x + v.x, y + v.y)

  def abs: Double = math.sqrt(x * x + y * y)

  def normalized: V2d =
    val magnitude = abs
    if (magnitude != 0) V2d(x / magnitude, y / magnitude)
    else this // oppure throw un'eccezione, oppure Option[V2d]

  def mul(factor: Double): V2d = V2d(x * factor, y * factor)

  override def toString: String = s"V2d($x, $y)"