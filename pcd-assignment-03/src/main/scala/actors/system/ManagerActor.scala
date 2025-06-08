package actors.system

import actors.model.{Boid, BoidsModel}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ManagerActor:
  def apply(model: BoidsModel): Behavior[ManagerCommand] =
    Behaviors.setup { context =>
      val boidActors = model.boids.map(b =>
        b.id -> context.spawn(BoidActor(b), s"boid-${b.id}")
      ).toMap
      context.self ! StartCycle
      cycle(model, boidActors)
    }

  private def cycle(model: BoidsModel, boids: Map[Int, ActorRef[BoidCommand]]): Behavior[ManagerCommand] =
    Behaviors.receive: (context, msg) =>
      msg match
        case StartCycle =>
          context.log.info("Start vel calc")
          model.boids.foreach(b => boids(b.id) ! CalculateVelocity(model, context.messageAdapter(VelocityDone)))
          awaitingVelocities(model, model.boids.size, Map(), boids)

        case _ => Behaviors.unhandled


  private def awaitingVelocities(model: BoidsModel,
                                 remaining: Int,
                                 updatedBoids: Map[Int, Boid],
                                 boidActors: Map[Int, ActorRef[BoidCommand]]): Behavior[ManagerCommand] =
    Behaviors.receive: (context, msg) =>
      msg match
        case VelocityDone(VelocityComputed(id, boid)) =>
          val newRemaining = remaining - 1
          val newMap = updatedBoids + (id -> boid)
          if (newRemaining == 0) {
            context.log.info("All boids calculated vel")
            context.log.info("Start pos calc")
            model.boids.foreach(b => boidActors(b.id) ! CalculatePosition(model, context.messageAdapter(PositionDone)))
            awaitingPositions(model, model.boids.size, Map(), boidActors)
          } else
            awaitingVelocities(model, newRemaining, newMap, boidActors)

        case _ => Behaviors.unhandled



  private def awaitingPositions(model: BoidsModel,
                                remaining: Int,
                                updatedBoids: Map[Int, Boid],
                                boidActors: Map[Int, ActorRef[BoidCommand]]): Behavior[ManagerCommand] =
    Behaviors.receive: (context, msg) =>
      msg match
        case PositionDone(PositionComputed(id, boid)) =>
          val newRemaining = remaining - 1
          val newMap = updatedBoids + (id -> boid)
          if (newRemaining == 0) {
            context.log.info("All boids calculated pos")
            //TODO: call the GUI here
            context.self ! StartCycle
            cycle(model, boidActors)
          } else
            awaitingPositions(model, newRemaining, updatedBoids, boidActors)

        case _ => Behaviors.unhandled