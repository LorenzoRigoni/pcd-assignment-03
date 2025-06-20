package it.unibo.agar.model

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.WorldProtocol.WorldMessage

object CollisionManagerActor:

  import it.unibo.agar.PlayerProtocol.*

  def apply(playerId: String, scoreManager: ActorRef[PlayerMessage], worldManager: ActorRef[WorldMessage]) : Behavior[PlayerMessage] =
    Behaviors.receiveMessage[PlayerMessage]:
      case FoodCollision(food) => ???
      case PlayerCollision(player) => ???
      case _ => Behaviors.unhandled

