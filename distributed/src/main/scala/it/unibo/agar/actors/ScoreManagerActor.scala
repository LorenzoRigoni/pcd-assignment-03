package it.unibo.agar.actors

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

    context.log.info(s"ScoreManagerActor for $playerId initialized with score = $initialScore")

    Behaviors.receiveMessage {
      case PlayerProtocol.CurrentScore(newScore) =>
        score += newScore
        context.log.info(s"[$playerId] Score increased by $newScore → total = $score")
        worldManager ! UpdatePlayerScore(playerId, score)

        if score >= winScore && !hasWon then
          hasWon = true
          context.log.info(s"[$playerId] WIN CONDITION MET with score = $score!")
          worldManager ! NotifyVictory(playerId, score)

        Behaviors.same

      case RequestCurrentMass(replyTo) =>
        context.log.debug(s"[$playerId] Mass requested → replying with score = $score")
        replyTo ! PlayerProtocol.CurrentMass(score)
        Behaviors.same

      case other =>
        context.log.warn(s"[$playerId] Received unexpected message: $other")
        Behaviors.unhandled
    }
  }