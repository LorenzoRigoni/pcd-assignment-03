package it.unibo.agar.model

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.PlayerProtocol
import it.unibo.agar.WorldProtocol.WorldMessage

object ScoreManagerActor:
  import it.unibo.agar.PlayerProtocol.PlayerMessage
  
  def apply(playerId: String, initialScore: Double, worldManager: ActorRef[WorldMessage]) : Behavior[PlayerMessage] = Behaviors.setup { context => 
    var score = initialScore
    Behaviors.receiveMessage {
      case PlayerProtocol.CurrentScore(score) => ???
      case _ => Behaviors.unhandled  
    }
  }
