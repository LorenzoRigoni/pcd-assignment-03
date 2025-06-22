package it.unibo.agar.model

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.PlayerProtocol
import it.unibo.agar.PlayerProtocol.RequestCurrentMass
import it.unibo.agar.WorldProtocol.{NotifyVictory, UpdatePlayerScore, WorldMessage}

object ScoreManagerActor:
  import it.unibo.agar.PlayerProtocol.PlayerMessage
  
  def apply(playerId: String, initialScore: Double, winScore: Double ,worldManager: ActorRef[WorldMessage]) : Behavior[PlayerMessage] = Behaviors.setup { context =>
    var score = initialScore
    var hasWon = false
    Behaviors.receiveMessage {
      //aggiorna il punteggio, lo notica al mondo, controlla se è sufficiente per vincere e nel caso notifica al World anche la vittoria
      case PlayerProtocol.CurrentScore(newScore) =>
        score += newScore
        worldManager ! UpdatePlayerScore(playerId, score)
        if score >= winScore && !hasWon then
          hasWon = true
          worldManager ! NotifyVictory(playerId, score)
        Behaviors.same
      //comunica l'attuale punteggio/massa al collisionManagerActor
      case RequestCurrentMass(replyTo) =>
            replyTo ! PlayerProtocol.CurrentMass(score)
            Behaviors.same
      case _ => Behaviors.unhandled  
    }
  }
