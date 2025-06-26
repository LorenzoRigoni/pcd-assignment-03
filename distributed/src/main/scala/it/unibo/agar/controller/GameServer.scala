package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import com.typesafe.config.ConfigFactory
import it.unibo.agar.actors.{ViewActor, WorldManagerActor}
import it.unibo.agar.model.{GameInitializer, World}
import it.unibo.agar.GameConf.*

object GameServer extends App:
  val config = ConfigFactory.load("server")
  val system = ActorSystem(Behaviors.empty, "agario", config)

  val world = World(worldWidth, worldHeight, Seq.empty, GameInitializer.initialFoods(numFood, worldWidth, worldHeight, foodMass))

  val worldManager = ClusterSingleton(system).init(
    SingletonActor(WorldManagerActor(world), "WorldManager")
  )

