package it.unibo.agar.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.GameConf.*
import it.unibo.agar.WorldProtocol.*
import it.unibo.agar.model.{EatingManager, Player, World}
import it.unibo.agar.{PlayerProtocol, ViewProtocol}

import scala.concurrent.duration.*

object WorldManagerActor:
  def apply(initialWorld: World): Behavior[WorldMessage] =
    Behaviors.setup { context =>
      import context.executionContext

      var world = initialWorld
      var players: Map[String, ActorRef[PlayerProtocol.PlayerMessage]] = Map.empty //mappa playerID -> ActorRef Player actor
      var views: Map[String, ActorRef[ViewProtocol.ViewMessage]] = Map.empty //mappa playerID -> ActorRef view Actor

      def generateRandomFood(): it.unibo.agar.model.Food = {
        it.unibo.agar.model.Food(
          id = java.util.UUID.randomUUID().toString,
          x = scala.util.Random.nextDouble() * worldWidth,
          y = scala.util.Random.nextDouble() * worldHeight,
          mass = foodMass
        )
      }

      //controllo collisioni e aggiornamento view ogni 50 ms
      context.system.scheduler.scheduleWithFixedDelay(0.millis, 50.millis) { () =>
        world.players.foreach { player =>
          world.foods.foreach { food =>
            if EatingManager.collides(player, food) then
              players.get(player.id).foreach { playerRef =>
                playerRef ! PlayerProtocol.FoodCollision(food,player.x, player.y)
              }
          }

          world.players.filterNot(_.id == player.id).foreach { otherPlayer =>
            if EatingManager.collides(player, otherPlayer) then
              players.get(player.id).foreach { playerRef =>
                playerRef ! PlayerProtocol.PlayerCollision(otherPlayer,player.x, player.y)
              }
          }
        }

        views.values.foreach { viewRef =>
          viewRef ! ViewProtocol.UpdateView(world)
        }
      }

      context.system.scheduler.scheduleWithFixedDelay(0.seconds, 1.second) { () =>
        context.self ! GenerateFood
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
          /*world = world.copy(players = world.players.map {
            case p if p.id == playerId => p.copy(x = x, y = y)
            case other => other
          })*/
          world.playerById(playerId).foreach: p =>
            val updatedPlayer = p.copy(x = x, y = y)
            world = world.updatePlayer(updatedPlayer)

          Behaviors.same

        case GenerateFood =>
          context.log.info("Num foods: " + world.foods.size)
          if (world.foods.size < numFood) {
            val newFood = generateRandomFood()
            world = world.copy(foods = world.foods :+ newFood)
            context.log.info(s"Generated new food ${newFood.id}")
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
          world.playerById(playerId).foreach { p =>
            val updatedPlayer = p.copy(mass = newScore)
            world = world.updatePlayer(updatedPlayer)
          }
          Behaviors.same

        case NotifyVictory(playerId, score) =>
          context.log.info(s"PLAYER $playerId WON! SCORE: $score")
          Behaviors.stopped

        case Stop =>
          context.log.info("Stopping WorldManagerActor via stop message")
          Behaviors.stopped  
      }
    }
