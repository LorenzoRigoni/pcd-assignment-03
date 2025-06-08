package actors.model

final case class BoidsModel(
                             boids: List[Boid],
                             separationWeight: Double,
                             alignmentWeight: Double,
                             cohesionWeight: Double,
                             width: Double,
                             height: Double,
                             maxSpeed: Double,
                             perceptionRadius: Double,
                             avoidRadius: Double
                           ):

  def minX: Double = -width / 2
  def maxX: Double = width / 2
  def minY: Double = -height / 2
  def maxY: Double = height / 2

  def createBoids(num: Int): BoidsModel = {
    val newBoids = List.tabulate(num) { i =>
      val pos = P2d(-width / 2 + math.random() * width, -height / 2 + math.random() * height)
      val vel = V2d(math.random() * maxSpeed / 2 - maxSpeed / 4, math.random() * maxSpeed / 2 - maxSpeed / 4)
      Boid(i, pos, vel)
    }
    this.copy(boids = newBoids)
  }

  def withSeparationWeight(w: Double): BoidsModel = copy(separationWeight = w)
  def withAlignmentWeight(w: Double): BoidsModel = copy(alignmentWeight = w)
  def withCohesionWeight(w: Double): BoidsModel = copy(cohesionWeight = w)
