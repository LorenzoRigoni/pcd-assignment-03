package it.unibo.agar.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.WorldProtocol.UpdatePlayerMovement
import it.unibo.agar.{PlayerProtocol, WorldProtocol}

object PlayerMovementActor:
  import it.unibo.agar.PlayerProtocol.PlayerMessage

  def apply(playerId: String,
            initialX: Double,
            initialY: Double,
            worldWidth: Int,
            worldHeight: Int,
            speed: Double,
            playerActor: ActorRef[PlayerProtocol.PlayerMessage],
            worldManager: ActorRef[WorldProtocol.WorldMessage]): Behavior[PlayerMessage] =
    Behaviors.setup{ context =>
      var x = initialX
      var y = initialY
      var direction: (Double, Double) = (0.0, 0.0)
      var mass: Double = 120.0

      Behaviors.receiveMessage{
        //Cambio di direzione del player 
        case PlayerProtocol.Move(newDx, newDy) => 
          direction = (newDx, newDy)
          Behaviors.same
        //aggiornamento della posizione e notifica al World  
        case PlayerProtocol.Tick =>
          val (dx, dy) = direction
          x = (x + dx * speed).max(0).min(worldWidth)
          y = (y + dy * speed).max(0).min(worldHeight)
          worldManager ! UpdatePlayerMovement(playerId, x, y)
          playerActor ! PlayerProtocol.CurrentPosition(x,y)
          Behaviors.same
        case _ => Behaviors.unhandled
      }
    }
