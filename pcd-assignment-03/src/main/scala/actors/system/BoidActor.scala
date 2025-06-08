package actors.system

import actors.model.Boid
import actors.*
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object BoidActor:
  def apply(initialBoid: Boid): Behavior[BoidCommand] =
    Behaviors.setup { context =>
      behavior(initialBoid)
    }

  private def behavior(boid: Boid): Behavior[BoidCommand] =
    Behaviors.receive: (context, message) =>
      message match 
        case CalculateVelocity(model, replyTo) =>
          val updated = boid.updateVelocity(model)
          replyTo ! VelocityComputed(boid.id, updated)
          behavior(updated)

        case CalculatePosition(model, replyTo) =>
          val updated = boid.updatePosition(model)
          replyTo ! PositionComputed(boid.id, updated)
          behavior(updated)