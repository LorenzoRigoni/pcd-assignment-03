package it.unibo.agar.actors

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.{MemberEvent, MemberRemoved, MemberUp, ReachabilityEvent, ReachableMember, UnreachableMember}
import akka.cluster.typed.{Cluster, ClusterSingleton, SingletonActor, Subscribe}
import it.unibo.agar.model.World

import scala.concurrent.duration.*

object ClusterSupervisorActor:
  private trait ClusterCommand

  private case class MemberEventWrapper(event: MemberEvent) extends ClusterCommand

  private case class ReachabilityEventWrapper(event: ReachabilityEvent) extends ClusterCommand

  def apply(initialWorld: World): Behavior[Nothing] =
    Behaviors
      .supervise(clusterBehavior(initialWorld))
      .onFailure(SupervisorStrategy.restart.withLimit(10, 30.seconds))
      .narrow

  private def clusterBehavior(initialWorld: World): Behavior[ClusterCommand] =
    Behaviors.setup { ctx =>
      val cluster = Cluster(ctx.system)

      if cluster.selfMember.roles.contains("server") then
        ClusterSingleton(ctx.system).init(
          SingletonActor(
            Behaviors.supervise(WorldManagerActor(initialWorld))
              .onFailure[Exception](SupervisorStrategy.restart),
            "WorldManager"
          )
        )

        val memberAdapter = ctx.messageAdapter[MemberEvent](MemberEventWrapper)
        val reachabilityAdapter = ctx.messageAdapter[ReachabilityEvent](ReachabilityEventWrapper)

        cluster.subscriptions ! Subscribe(memberAdapter, classOf[MemberEvent])
        cluster.subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

        Behaviors.receiveMessage {
          case MemberEventWrapper(MemberUp(member)) =>
            ctx.log.info(s"Member is up: ${member.address}")
            Behaviors.same

          case MemberEventWrapper(MemberRemoved(member, previousStatus)) =>
            ctx.log.info(s"Member removed: ${member.address} after $previousStatus")
            Behaviors.same

          case ReachabilityEventWrapper(UnreachableMember(member)) =>
            ctx.log.warn(s"Member unreachable: ${member.address}")
            Behaviors.same

          case ReachabilityEventWrapper(ReachableMember(member)) =>
            ctx.log.info(s"Member reachable again: ${member.address}")
            Behaviors.same

          case _ =>
            Behaviors.same
        }
      else
        // Se non è un server, può comunque attivare un comportamento passivo
        Behaviors.receiveMessage(_ => Behaviors.same)
    }