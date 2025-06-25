package it.unibo.agar.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.GameConf._
import it.unibo.agar.WorldProtocol.*
import it.unibo.agar.model.World
import it.unibo.agar.{PlayerProtocol, ViewProtocol}

import scala.concurrent.duration._

object WorldManagerActor:
  def apply(initialWorld: World): Behavior[WorldMessage] =
    Behaviors.setup { context =>
      import context.executionContext

      var world = initialWorld
      var players: Map[String, ActorRef[PlayerProtocol.PlayerMessage]] = Map.empty //mappa playerID -> ActorRef Player actor
      var views: Map[String, ActorRef[ViewProtocol.ViewMessage]] = Map.empty //mappa playerID -> ActorRef view Actor

      context.system.scheduler.scheduleWithFixedDelay(0.millis, 50.millis) { () =>
        views.values.foreach { viewRef =>
          viewRef ! ViewProtocol.UpdateView(world)
        }
      }
      
      Behaviors.receiveMessage {
        case RegisterPlayer(playerId, playerActorRef) =>
          context.log.info(s"Registering player $playerId")
          val newPlayer = it.unibo.agar.model.Player(
            id = playerId,
            x = scala.util.Random.nextDouble() * worldWidth,
            y = scala.util.Random.nextDouble() * worldHeight,
            initialPlayerMass)
          players = players + (playerId -> playerActorRef)
          world = world.copy(players = world.players :+ newPlayer)
          Behaviors.same

        case RegisterView(playerId, viewRef) =>
          context.log.info(s"Registering world for plater $playerId")
          views = views + (playerId -> viewRef)
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
          views = views - playerId
          world = world.removePlayersById(Seq(playerId))
          Behaviors.same

        case UpdatePlayerScore(playerId, newScore) =>
          Behaviors.same

        case NotifyVictory(playerId, score) =>
          context.log.info(s"Player $playerId wins! Score: $score")
          Behaviors.stopped

        case Stop =>
          context.log.info("Stopping WorldManagerActor via stop message")
          Behaviors.stopped  
      }
    }
