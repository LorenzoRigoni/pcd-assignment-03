package it.unibo.agar.controller
import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.cluster.ClusterEvent
import akka.cluster.typed.*
import com.typesafe.config.ConfigFactory
import it.unibo.agar.GameConf.*
import it.unibo.agar.WorldProtocol
import it.unibo.agar.actors.{PlayerActor, WorldManagerActor}
import it.unibo.agar.model.World

import scala.util.Random

object Main:

  sealed trait InternalCommand
  private final case class WrappedClusterEvent(event: ClusterEvent.MemberEvent) extends InternalCommand

  def apply(): Behavior[InternalCommand] = Behaviors.setup { context =>
    val cluster = Cluster(context.system)

    // Adattatore per eventi cluster
    val clusterEventAdapter = context.messageAdapter[ClusterEvent.MemberEvent](WrappedClusterEvent)
    cluster.subscriptions ! Subscribe(clusterEventAdapter, classOf[ClusterEvent.MemberEvent])

    val system = context.system

    val worldManager = ClusterSingleton(system).init(
      SingletonActor(WorldManagerActor(World(800, 600, Seq.empty, Seq.empty)), "WorldManager")
        .withStopMessage(WorldProtocol.NotifyVictory("dummy", 0))
    )

    // Crea il PlayerActor
    val playerName = s"Player-${Random.between(1000, 9999)}"
    context.spawn(
      PlayerActor(playerName,Random.nextInt(worldWidth), Random.nextInt(worldHeight), initialPlayerMass, worldManager), 
      playerName
    )

    Behaviors.receiveMessage {
      case WrappedClusterEvent(event) =>
        context.log.info(s"Evento cluster: $event")
        Behaviors.same
    }
  }

  @main def run(): Unit =
    val config = ConfigFactory.load("agario")
    ActorSystem(Main(), "agario", config)
