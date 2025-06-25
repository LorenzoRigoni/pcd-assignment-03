package it.unibo.agar

import akka.actor.typed.ActorRef
import it.unibo.agar.PlayerProtocol.CurrentScore
import it.unibo.agar.model.{Food, Player, World}

/** Tag interface for all messages sends by actors */
trait Message

object WorldProtocol:
  trait WorldMessage extends Message
  case class UpdatePlayerMovement(playerId: String, x: Double, y:Double) extends WorldMessage
  case class RemoveFood(foodId: String) extends WorldMessage
  case class RemovePlayer(playerId: String) extends WorldMessage
  case class UpdatePlayerScore(playerId: String, newScore: Double) extends WorldMessage
  case class NotifyVictory(playerId: String, score: Double) extends WorldMessage
  case class RegisterView(playerId: String, viewRef: ActorRef[ViewProtocol.ViewMessage]) extends WorldMessage
  case object Stop extends WorldMessage
 

object PlayerProtocol:
  trait PlayerMessage extends Message
  case class Move(dx: Double, dy: Double) extends PlayerMessage
  case object Tick extends PlayerMessage
  case class FoodCollision(food: Food) extends PlayerMessage
  case class PlayerCollision(player: Player) extends PlayerMessage
  case class CurrentScore(score: Double) extends PlayerMessage
  case class RequestCurrentMass(replyTo: ActorRef[CurrentMass]) extends PlayerMessage //interaction with request-response
  case class CurrentMass(mass: Double) extends PlayerMessage
  case class CurrentPosition(x: Double, y:Double) extends PlayerMessage


object ViewProtocol:
  trait ViewMessage extends Message
  case class MoveInput(dx: Double, dy: Double) extends ViewMessage //rileva input mouse
  case class UpdateView(world: World) extends ViewMessage

  //case object Tick extends ViewMessage

