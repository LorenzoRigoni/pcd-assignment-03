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
      var currentPlayerX: Double= 0
      var currentPlayerY: Double = 0
      var eatenPlayers: Set[String] = Set.empty

      Behaviors.receiveMessage {
        case FoodCollision(food, x, y) =>
          pendingFood = Some(food)
          currentPlayerX = x
          currentPlayerY = y
          scoreManager ! RequestCurrentMass(context.self)
          Behaviors.same

        case PlayerCollision(otherPlayer, x, y) =>
          if eatenPlayers.contains(otherPlayer.id) then
            context.log.debug(s"Ignoring repeated collision with already eaten player ${otherPlayer.id}")
            Behaviors.same
          else
            pendingPlayer = Some(otherPlayer)
            currentPlayerX = x
            currentPlayerY = y
            scoreManager ! RequestCurrentMass(context.self)
            Behaviors.same

        case CurrentMass(mass) =>
          pendingFood.foreach { food =>
            if EatingManager.canEatFood(Player(playerId, currentPlayerX, currentPlayerY, mass), food) then //passare coordinate player
              context.log.info(s"Player $playerId ate food ${food.id}")
              scoreManager ! CurrentScore(food.mass)
              worldManager ! RemoveFood(food.id)
          }
          pendingFood = None

          pendingPlayer.foreach { other =>
            if EatingManager.canEatPlayer(Player(playerId, currentPlayerX, currentPlayerY, mass), other) then //passare coordinate player
              context.log.info(s"Player $playerId ate player ${other.id}")
              scoreManager ! CurrentScore(other.mass)
              worldManager ! RemovePlayer(other.id)
              eatenPlayers += other.id
          }
          pendingPlayer = None
          Behaviors.same

        case _ => Behaviors.unhandled
      }
    }

