package it.unibo.agar.controller

import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import it.unibo.agar.actors.ClusterSupervisorActor
import it.unibo.agar.model.World
import it.unibo.agar.actors.{WorldManagerActor, PlayerActor}
import it.unibo.agar.startupWithRole

import scala.io.StdIn

object GameClient:
  def main(args: Array[String]): Unit =
    val playerId = if args.nonEmpty then args(0) else {
      println("Enter player ID:")
      StdIn.readLine()
    }
    
    val initialWorld = World(800, 600, Seq.empty, Seq.empty)

    // Avvia il sistema come client
    val system = startupWithRole("client", 0)(ClusterSupervisorActor(initialWorld))

    // Ottieni il singleton WorldManager (proxy)
    val worldManagerProxy = ClusterSingleton(system).init(
      SingletonActor(WorldManagerActor(initialWorld), "WorldManager")
    )

    // Crea PlayerActor
    val playerActor = system.systemActorOf(
      PlayerActor(playerId, 100, 100, 120, worldManagerProxy),
      s"player-$playerId"
    )

    println(s"Player $playerId connected. Press ENTER to exit.")
    StdIn.readLine()
    system.terminate()