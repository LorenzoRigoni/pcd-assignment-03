package actors

final case class Boid(pos: P2d, vel: V2d):

  def updateVelocity(model: BoidsModel): Boid =
    val nearby = getNearbyBoids(model)

    val separation = calculateSeparation(nearby, model)
    val alignment = calculateAlignment(nearby, model)
    val cohesion = calculateCohesion(nearby, model)

    val newVel = vel
      .sum(alignment.mul(model.alignmentWeight))
      .sum(separation.mul(model.separationWeight))
      .sum(cohesion.mul(model.cohesionWeight))

    val speed = newVel.abs
    val cappedVel =
      if speed > model.maxSpeed then newVel.normalized.mul(model.maxSpeed)
      else newVel

    this.copy(vel = cappedVel)

  def updatePosition(model: BoidsModel): Boid =
    val moved = pos.sum(vel)

    val xWrapped =
      if moved.x < model.minX then moved.sum(V2d(model.width, 0))
      else if moved.x >= model.maxX then moved.sum(V2d(-model.width, 0))
      else moved

    val wrapped =
      if xWrapped.y < model.minY then xWrapped.sum(V2d(0, model.height))
      else if xWrapped.y >= model.maxY then xWrapped.sum(V2d(0, -model.height))
      else xWrapped

    this.copy(pos = wrapped)

  private def getNearbyBoids(model: BoidsModel): List[Boid] =
    model.boids.filter { b =>
      b != this && pos.distance(b.pos) < model.perceptionRadius
    }

  private def calculateAlignment(nearby: List[Boid], model: BoidsModel): V2d =
    if nearby.isEmpty then V2d(0, 0)
    else
      val (sumX, sumY) = nearby.map(_.vel).foldLeft((0.0, 0.0)) {
        case ((accX, accY), v) => (accX + v.x, accY + v.y)
      }
      val avg = V2d(sumX / nearby.size, sumY / nearby.size)
      V2d(avg.x - vel.x, avg.y - vel.y).normalized

  private def calculateCohesion(nearby: List[Boid], model: BoidsModel): V2d =
    if nearby.isEmpty then V2d(0, 0)
    else
      val (sumX, sumY) = nearby.map(_.pos).foldLeft((0.0, 0.0)) {
        case ((accX, accY), p) => (accX + p.x, accY + p.y)
      }
      val center = P2d(sumX / nearby.size, sumY / nearby.size)
      V2d(center.x - pos.x, center.y - pos.y).normalized

  private def calculateSeparation(nearby: List[Boid], model: BoidsModel): V2d =
    val close = nearby.filter(b => pos.distance(b.pos) < model.avoidRadius)
    if close.isEmpty then V2d(0, 0)
    else
      val (dx, dy) = close.map(_.pos).foldLeft((0.0, 0.0)) {
        case ((accX, accY), p) => (accX + (pos.x - p.x), accY + (pos.y - p.y))
      }
      V2d(dx / close.size, dy / close.size).normalized