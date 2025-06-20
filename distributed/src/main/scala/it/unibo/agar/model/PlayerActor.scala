package it.unibo.agar.model

import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.WorldProtocol
import akka.actor.typed.{ActorRef, Behavior}

object PlayerActor:

  import it.unibo.agar.PlayerProtocol.*
  

  def apply(playerId: String, initialX: Double, initialY: Double,
            initialScore: Double, worldManager: ActorRef[WorldProtocol.WorldMessage]) : Behavior[PlayerMessage] = ???
    //Behaviors.setup[PlayerMessage] : context => { }
      //creo i tre sottoattori che gestiscono collisioni, punteggio e movimento del player
    Behaviors.receiveMessage[PlayerMessage]:
      case Move(x,y) => ???
      case Tick => ???
      case FoodCollision(food) => ???
      case PlayerCollision(player) => ???
      case CurrentScore(score) => ???
