package it.unibo.agar.model

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.PlayerProtocol
import it.unibo.agar.WorldProtocol.{NotifyVictory, PlayerRegistered, RegisterPlayer, RemoveFood, RemovePlayer, UpdatePlayerMovement, UpdatePlayerScore, WorldMessage}

object WorldManager:
  def apply(initialWorld: World, victoryScore: Double = 1000.0): Behavior[WorldMessage] =
    Behaviors.setup { context =>
      var world = initialWorld
      var players: Map[String, ActorRef[PlayerProtocol.PlayerMessage]] = Map.empty //mappa playerID -> ActorRef


      Behaviors.receiveMessage {
        case RegisterPlayer(playerId, x, y, mass, replyTo) =>
          val playerRef = context.spawn(PlayerActor(playerId, x, y, mass, context.self), s"player-$playerId")
          players = players + (playerId -> playerRef)
          world = world.updatePlayer(Player(playerId, x, y, mass))
          replyTo ! PlayerRegistered(playerRef)
          Behaviors.same

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
          world = world.removePlayersById(Seq(playerId))
          Behaviors.same

        case UpdatePlayerScore(playerId, newScore) =>
          Behaviors.same

        case NotifyVictory(playerId, score) =>
          context.log.info(s"Player $playerId wins! Score: $score")
          Behaviors.stopped
      }
    }
