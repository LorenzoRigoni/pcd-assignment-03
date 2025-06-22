package it.unibo.agar.model

import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.WorldProtocol
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.model.*

object PlayerActor:

  import it.unibo.agar.PlayerProtocol.*


  def apply(playerId: String, initialX: Double, initialY: Double,
            initialScore: Double, worldManager: ActorRef[WorldProtocol.WorldMessage]) : Behavior[PlayerMessage] =
    Behaviors.setup[PlayerMessage]: context =>
      var winScore = 1000
      var worldWidth: Int = 1000
      var worldHeight: Int = 1000
      var speed: Double = 10.0
  
      //creo i tre sottoattori che gestiscono collisioni, punteggio e movimento del player
      val scoreActor = context.spawn(ScoreManagerActor(playerId, initialScore, winScore, worldManager), s"points-$playerId")
      val collisionActor = context.spawn(CollisionManagerActor(playerId, scoreActor, worldManager), s"collisions-$playerId")
      val movementActor = context.spawn(PlayerMovementActor(playerId, initialX, initialY,worldWidth, worldHeight, speed, worldManager), s"move-$playerId")

      //smisto i messaggi tra i vari sottoattori
      Behaviors.receiveMessage[PlayerMessage]:
        case Move(x, y) => movementActor ! Move(x,y); Behaviors.same
        case Tick => movementActor ! Tick; Behaviors.same
        case FoodCollision(food) => collisionActor ! FoodCollision(food); Behaviors.same
        case PlayerCollision(player) => collisionActor ! PlayerCollision(player); Behaviors.same
        case CurrentScore(score) => scoreActor ! CurrentScore(score); Behaviors.same
