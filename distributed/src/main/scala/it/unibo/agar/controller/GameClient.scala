package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import com.typesafe.config.ConfigFactory
import it.unibo.agar.GameConf._
import it.unibo.agar.actors.{PlayerActor, ViewActor, WorldManagerActor}
import it.unibo.agar.model.World
import it.unibo.agar.{GameConf, WorldProtocol}

import scala.io.StdIn
import scala.util.Random

object GameClient:
  private def getPlayerId(args: Array[String]): String =
    if args.nonEmpty && args(0).trim.nonEmpty then args(0).trim
    else {
      println("Enter player ID:")
      val input = scala.io.StdIn.readLine().trim
      if input.isEmpty then "Player1" else input
    }

  def main(args: Array[String]): Unit =
    val playerId = getPlayerId(args)

    val config = ConfigFactory.load("client")
    val system = ActorSystem(Behaviors.empty, "agario", config)

    val worldManager = ClusterSingleton(system).init(
      SingletonActor(WorldManagerActor(World(800, 600, Seq.empty, Seq.empty)), "WorldManager")
    )

    // Step 1: Ricevo info iniziali dal server
    val initReceiver = Behaviors.receiveMessage[WorldProtocol.InitialPlayerInfo] {
      case WorldProtocol.InitialPlayerInfo(x, y, mass) =>
        val player = system.systemActorOf(
          PlayerActor(playerId, x, y, mass, worldManager),
          s"player-$playerId"
        )

        val view = system.systemActorOf(
          ViewActor(playerId, player, worldManager),
          s"view-$playerId"
        )

        // Dopo aver creato gli attori, li registriamo
        worldManager ! WorldProtocol.RegisterPlayerActor(playerId, player)
        worldManager ! WorldProtocol.RegisterView(playerId, view)

        Behaviors.stopped
    }
    val receiver = system.systemActorOf(initReceiver, s"init-receiver-$playerId")

    // Step 0: Chiedo inizializzazione al WorldManager
    worldManager ! WorldProtocol.RegisterPlayer(playerId, receiver)

