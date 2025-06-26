package it.unibo.agar.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.ViewProtocol
import it.unibo.agar.view.GlobalView
import it.unibo.agar.model.World

object GlobalViewActor:
  def apply(): Behavior[ViewProtocol.ViewMessage] =
    Behaviors.setup { context =>

      val globalView = new GlobalView()
      globalView.visible = true

      Behaviors.receiveMessage {
        case ViewProtocol.UpdateView(world) =>
          globalView.updateWorld(world)
          Behaviors.same
      }
    }