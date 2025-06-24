package it.unibo.agar.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.WorldProtocol.{RemoveFood, RemovePlayer, WorldMessage}
import it.unibo.agar.model.{EatingManager, Food, Player}

object CollisionManagerActor:

  import it.unibo.agar.PlayerProtocol.*

  def apply(playerId: String,
            scoreManager: ActorRef[PlayerMessage],
            worldManager: ActorRef[WorldMessage]) : Behavior[PlayerMessage] =
    Behaviors.setup { context =>
      var pendingFood: Option[Food] = None
      var pendingPlayer: Option[Player] = None

      Behaviors.receiveMessage {
        case FoodCollision(food) =>
          pendingFood = Some(food)
          scoreManager ! RequestCurrentMass(context.self)
          Behaviors.same

        case PlayerCollision(otherPlayer) =>
          pendingPlayer = Some(otherPlayer)
          scoreManager ! RequestCurrentMass(context.self)
          Behaviors.same

        case CurrentMass(mass) =>
          pendingFood.foreach { food =>
            if EatingManager.canEatFood(Player(playerId, 0, 0, mass), food) then
              context.log.info(s"Player $playerId ate food ${food.id}")
              scoreManager ! CurrentScore(food.mass)
              worldManager ! RemoveFood(food.id)
          }
          pendingFood = None

          pendingPlayer.foreach { other =>
            if EatingManager.canEatPlayer(Player(playerId, 0, 0, mass), other) then
              context.log.info(s"Player $playerId ate player ${other.id}")
              scoreManager ! CurrentScore(other.mass)
              worldManager ! RemovePlayer(other.id)
          }
          pendingPlayer = None

          Behaviors.same

        case _ => Behaviors.unhandled
      }
    }

