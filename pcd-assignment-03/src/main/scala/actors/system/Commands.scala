package actors.system

import actors.model.{Boid, BoidsModel}
import akka.actor.typed.ActorRef

sealed trait BoidCommand
case class CalculateVelocity(model: BoidsModel, replyTo: ActorRef[VelocityComputed]) extends BoidCommand
case class CalculatePosition(model: BoidsModel, replyTo: ActorRef[PositionComputed]) extends BoidCommand

case class VelocityComputed(id: Int, boid: Boid)
case class PositionComputed(id: Int, boid: Boid)

sealed trait ManagerCommand
case object StartCycle extends ManagerCommand
case class VelocityDone(result: VelocityComputed) extends ManagerCommand
case class PositionDone(result: PositionComputed) extends ManagerCommand