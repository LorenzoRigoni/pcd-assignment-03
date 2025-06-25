package it.unibo.agar.controller

import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import it.unibo.agar.actors.{ClusterSupervisorActor, PlayerActor, ViewActor, WorldManagerActor}
import it.unibo.agar.model.World
import it.unibo.agar.{WorldProtocol, startupWithRole}

import scala.io.StdIn

object GameClient:
  def main(args: Array[String]): Unit =
    val inputPlayerId = if args.nonEmpty then args(0) else {
      println("Enter player ID:")
      StdIn.readLine()
    }
    
    val initialWorld = World(800, 600, Seq.empty, Seq.empty)

    // Avvia il sistema come client
    val system = startupWithRole("client", 0)(ClusterSupervisorActor(initialWorld))

    // per ottenere il singleton WorldManager (proxy)
    val worldManagerProxy = ClusterSingleton(system).init(
      SingletonActor(WorldManagerActor(initialWorld), "WorldManager")
    )

    // Crea PlayerActor
    val playerId = inputPlayerId.replaceAll("\\s+", "_") //per evitare eccezioni date da spazi bianchi...
    val playerActor = system.systemActorOf(
      PlayerActor(playerId, 100, 100, 120, worldManagerProxy),
      s"player-$playerId"
    )
    worldManagerProxy ! WorldProtocol.RegisterPlayer(playerId, playerActor)

    //Crea ViewActor
    val viewActor = system.systemActorOf(
      ViewActor(playerId, playerActor, worldManagerProxy),
      s"view-$playerId"
    )
    worldManagerProxy ! WorldProtocol.RegisterView(playerId, viewActor)

    println(s"Player $playerId connected. Press ENTER to exit.")
    StdIn.readLine()
    system.terminate()