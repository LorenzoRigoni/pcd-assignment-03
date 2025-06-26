package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import com.typesafe.config.ConfigFactory
import it.unibo.agar.actors.{ClusterSupervisorActor, PlayerActor, ViewActor, WorldManagerActor}
import it.unibo.agar.model.{GameInitializer, World}
import it.unibo.agar.{GameConf, WorldProtocol, startupWithRole}
import it.unibo.agar.GameConf.*

import scala.io.StdIn

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

    val player = system.systemActorOf(
      PlayerActor(playerId, 100, 100, GameConf.initialPlayerMass, worldManager),
      s"player-$playerId"
    )

    val view = system.systemActorOf(
      ViewActor(playerId, player, worldManager),
      s"view-$playerId"
    )

    worldManager ! WorldProtocol.RegisterPlayer(playerId, player)
    worldManager ! WorldProtocol.RegisterView(playerId, view)

    println(s"Player $playerId connected. Press ENTER to exit.")
    StdIn.readLine()
    system.terminate()
