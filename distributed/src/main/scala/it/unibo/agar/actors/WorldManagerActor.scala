package it.unibo.agar.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.WorldProtocol.*
import it.unibo.agar.model.World
import it.unibo.agar.{PlayerProtocol, ViewProtocol}

object WorldManagerActor:
  def apply(initialWorld: World, victoryScore: Double = 1000.0): Behavior[WorldMessage] =
    Behaviors.setup { context =>
      var world = initialWorld
      var players: Map[String, ActorRef[PlayerProtocol.PlayerMessage]] = Map.empty //mappa playerID -> ActorRef Player actor
      var views: Map[String, ActorRef[ViewProtocol.ViewMessage]] = Map.empty //mappa playerID -> ActorRef view Actor


      Behaviors.receiveMessage {
        
        case UpdatePlayerMovement(playerId, x, y) =>
          players.get(playerId).foreach { playerRef =>
            playerRef ! PlayerProtocol.Move(x, y)
          }
          Behaviors.same

        case RemoveFood(foodId) =>
          world.foods.find(_.id == foodId).foreach { food =>
            world = world.removeFoods(Seq(food))
            context.log.info(s"Food $foodId removed")
          }
          Behaviors.same

        case RemovePlayer(playerId) =>
          players.get(playerId).foreach { playerRef =>
            context.stop(playerRef)
          }
          players = players - playerId
          views = views - playerId
          world = world.removePlayersById(Seq(playerId))
          Behaviors.same

        case UpdatePlayerScore(playerId, newScore) =>
          Behaviors.same

        case NotifyVictory(playerId, score) =>
          context.log.info(s"Player $playerId wins! Score: $score")
          Behaviors.stopped
      }
    }
