package it.unibo.agar

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory

import java.net.InetAddress

object GameConf:
  val seeds = List(2551, 2552) // seed used in the configuration
  val worldHeight = 1000
  val worldWidth = 1000
  val initialPlayerMass: Double = 120.0
  val numFood = 150
  val foodMass = 100.0

def startup[X](file: String = "base-cluster", port: Int)(root: => Behavior[X]): ActorSystem[X] =
  // Override the configuration of the port
  val config = ConfigFactory
    .parseString(s"""akka.remote.artery.canonical.port=$port""")
    .withFallback(ConfigFactory.load(file))

  // Create an Akka system
  ActorSystem(root, file, config)

def startupWithRole[X](role: String, port: Int)(root: => Behavior[X]): ActorSystem[X] =
  //val hostname = InetAddress.getLocalHost.getHostAddress
  val hostname = "127.0.0.1"
  val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.hostname = "$hostname"
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """)
    .withFallback(ConfigFactory.load("agario"))

  // Create an Akka system
  ActorSystem(root, "agario", config)
