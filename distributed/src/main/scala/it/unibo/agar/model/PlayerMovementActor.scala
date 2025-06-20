package it.unibo.agar.model

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.{PlayerProtocol, WorldProtocol}

object PlayerMovementActor:
  import it.unibo.agar.PlayerProtocol.PlayerMessage

  def apply(playerId: String, initialX: Double, initialY: Double, worldManager: ActorRef[WorldProtocol.WorldMessage]): Behavior[PlayerMessage] =
    Behaviors.setup{ context =>
      var x = initialX
      var y = initialY

      Behaviors.receiveMessage{
        case PlayerProtocol.Move(x, y) => ???
        case PlayerProtocol.Tick => ???
        case _ => Behaviors.unhandled
      }
    }
