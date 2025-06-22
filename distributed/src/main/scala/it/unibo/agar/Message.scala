package it.unibo.agar

import akka.actor.typed.ActorRef
import it.unibo.agar.model.{Food, Player}

/** Tag interface for all messages sends by actors */
trait Message

object WorldProtocol:
  trait WorldMessage extends Message
  case class UpdatePlayerMovement(playerId: String, x: Double, y:Double) extends WorldMessage
  case class RemoveFood(foodId: String) extends WorldMessage
  case class RemovePlayer(playerId: String) extends WorldMessage
  case class UpdatePlayerScore(playerId: String, newScore: Double) extends WorldMessage
  case class NotifyVictory(playerId: String, score: Double) extends WorldMessage
  case class RegisterPlayer(playerId: String, initialX: Double, initialY: Double, initialMass: Double = 100.0, replyTo: ActorRef[PlayerRegistered]) extends WorldMessage
  case class PlayerRegistered(playerRef: ActorRef[PlayerProtocol.PlayerMessage]) extends WorldMessage

object PlayerProtocol:
  trait PlayerMessage extends Message
  case class Move(x: Double, y: Double) extends PlayerMessage
  case object Tick extends PlayerMessage
  case class FoodCollision(food: Food) extends PlayerMessage
  case class PlayerCollision(player: Player) extends PlayerMessage
  case class CurrentScore(score: Double) extends PlayerMessage

