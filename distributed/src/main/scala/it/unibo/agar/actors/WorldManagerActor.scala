package it.unibo.agar.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.GameConf.*
import it.unibo.agar.ViewProtocol.DisplayVictory
import it.unibo.agar.WorldProtocol.*
import it.unibo.agar.model.{EatingManager, Player, World}
import it.unibo.agar.{PlayerProtocol, ViewProtocol}

import scala.concurrent.duration.*

object WorldManagerActor:
  def apply(initialWorld: World): Behavior[WorldMessage] =
    Behaviors.setup { context =>
      import context.executionContext

      var world = initialWorld
      var players: Map[String, ActorRef[PlayerProtocol.PlayerMessage]] = Map.empty //Map playerID -> ActorRef Player actor
      var views: Map[String, ActorRef[ViewProtocol.ViewMessage]] = Map.empty //Map playerID -> ActorRef view Actor
      val globalViewActor = context.spawn(GlobalViewActor(), "global-view")
      views += ("global" -> globalViewActor)


      def generateRandomFood(): it.unibo.agar.model.Food = {
        it.unibo.agar.model.Food(
          id = java.util.UUID.randomUUID().toString,
          x = scala.util.Random.nextDouble() * worldWidth,
          y = scala.util.Random.nextDouble() * worldHeight,
          mass = foodMass
        )
      }

      context.system.scheduler.scheduleWithFixedDelay(0.millis, 50.millis) { () =>
        world.players.foreach { player =>
          world.foods.foreach { food =>
            if EatingManager.collides(player, food) then
              players.get(player.id).foreach { playerRef =>
                playerRef ! PlayerProtocol.FoodCollision(food, player.x, player.y)
              }
          }

          world.players.filterNot(_.id == player.id).foreach { otherPlayer =>
            if EatingManager.collides(player, otherPlayer) then
              players.get(player.id).foreach { playerRef =>
                playerRef ! PlayerProtocol.PlayerCollision(otherPlayer, player.x, player.y)
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
        case RegisterPlayer(playerId, replyTo) =>
          context.log.info(s"Registering player $playerId")
          val x = scala.util.Random.nextDouble() * worldWidth
          val y = scala.util.Random.nextDouble() * worldHeight
          val newPlayer = Player(playerId, x, y, initialPlayerMass)

          world = world.copy(players = world.players :+ newPlayer)

          replyTo ! InitialPlayerInfo(x, y, initialPlayerMass)
          Behaviors.same

        case RegisterPlayerActor(playerId, actorRef) =>
          players = players + (playerId -> actorRef)
          Behaviors.same

        case RegisterView(playerId, viewRef) =>
          context.log.info(s"Registering world for plater $playerId")
          views = views + (playerId -> viewRef)
          Behaviors.same

        case UpdatePlayerMovement(playerId, x, y) =>
          world.playerById(playerId).foreach: p =>
            val updatedPlayer = p.copy(x = x, y = y)
            world = world.updatePlayer(updatedPlayer)
          Behaviors.same

        case GenerateFood =>
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
          context.log.info(s"Removing player $playerId")
          world = world.removePlayersById(Seq(playerId))
          players.get(playerId).foreach { playerRef =>
            playerRef ! PlayerProtocol.StopPlayer
          }
          players = players - playerId
          views = views - playerId
          Behaviors.same

        case UpdatePlayerScore(playerId, newScore) =>
          world.playerById(playerId).foreach { p =>
            val updatedPlayer = p.copy(mass = newScore)
            world = world.updatePlayer(updatedPlayer)
          }
          Behaviors.same

        case NotifyVictory(playerId, score) =>
          context.log.info(s"PLAYER $playerId WON! SCORE: $score")
          views.values.foreach {actor =>
            actor ! DisplayVictory(playerId, score)
          }
          Behaviors.stopped

        case Stop =>
          context.log.info("Stopping WorldManagerActor via stop message")
          Behaviors.stopped
      }
    }
