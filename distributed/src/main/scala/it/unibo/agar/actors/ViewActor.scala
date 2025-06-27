package it.unibo.agar.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.view.LocalView
import it.unibo.agar.{PlayerProtocol, ViewProtocol, WorldProtocol}

object ViewActor:

  def apply(playerId: String,
            playerActor: ActorRef[PlayerProtocol.PlayerMessage],
            worldManager: ActorRef[WorldProtocol.WorldMessage]
           ): Behavior[ViewProtocol.ViewMessage] =
    Behaviors.setup { context =>

      val view = new LocalView(context.self, playerId, playerActor)
      view.visible = true // per mostrare la finestra

      Behaviors.receiveMessage {

        case ViewProtocol.MoveInput(dx, dy) =>
          playerActor ! PlayerProtocol.Move(dx, dy)
          Behaviors.same

        case ViewProtocol.UpdateView(world) =>
          view.updateWorld(world) // aggiorna stato e chiama repaint()
          Behaviors.same

        case ViewProtocol.DisplayVictory(playerId, score) =>
          view.displayVictory(playerId, score)
          Behaviors.same
      }
    }