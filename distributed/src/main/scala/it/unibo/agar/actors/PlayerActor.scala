package it.unibo.agar.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.WorldProtocol
import it.unibo.agar.model.*
import scala.concurrent.duration.DurationInt

object PlayerActor:

  import it.unibo.agar.PlayerProtocol.*

  def apply(playerId: String, initialX: Double, initialY: Double,
            initialScore: Double, worldManager: ActorRef[WorldProtocol.WorldMessage]) : Behavior[PlayerMessage] =
    Behaviors.setup { context =>
      import context.executionContext
      val winScore = 1000
      val worldWidth: Int = 1000
      val worldHeight: Int = 1000
      val speed: Double = 10.0

      //creo i tre sottoattori che gestiscono collisioni, punteggio e movimento del player
      val scoreActor = context.spawn(ScoreManagerActor(playerId, initialScore, winScore, worldManager), s"points-$playerId")
      val collisionActor = context.spawn(CollisionManagerActor(playerId, scoreActor, worldManager), s"collisions-$playerId")
      val movementActor = context.spawn(PlayerMovementActor(playerId, initialX, initialY,worldWidth, worldHeight, speed,context.self, worldManager), s"move-$playerId")

      // Scheduler per il Tick
      context.system.scheduler.scheduleAtFixedRate(0.millis, 50.millis) { () =>
        context.self ! Tick
      }

      //smisto i messaggi tra i vari sottoattori
      Behaviors.receiveMessage{
        case Move(x, y) =>
          //context.log.info(s"PlayerActor: received Move($x, $y), forwarding to movementActor")
          movementActor ! Move(x, y)
          Behaviors.same

        case Tick =>
          //context.log.info("PlayerActor: Tick received, forwarding to movementActor")
          movementActor ! Tick
          Behaviors.same

        case FoodCollision(food, x, y) => 
          collisionActor ! FoodCollision(food, x, y)
          Behaviors.same
          
        case PlayerCollision(player, x, y) => 
          collisionActor ! PlayerCollision(player, x, y)
          Behaviors.same
          
        case CurrentScore(score) => 
          scoreActor ! CurrentScore(score)
          Behaviors.same
      }

    }