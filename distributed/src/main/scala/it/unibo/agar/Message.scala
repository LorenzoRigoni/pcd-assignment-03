package it.unibo.agar

import it.unibo.agar.model.{Food, Player}

/** Tag interface for all messages sends by actors */
trait Message

object WorldProtocol:
  case class UpdatePlayerMovement(playerId: String, x: Double, y:Double) extends Message
  case class RemoveFood(foodId: String) extends Message
  case class RemovePlayer(playerId: String) extends Message
  case class UpdatePlayerScore(playerId: String, newScore: Double) extends Message
  case class NotifyVictory(playerId: String, score: Double) extends Message

object PlayerProtocol:
  case class Move(x: Double, y: Double) extends Message
  case object Tick extends Message
  case class FoodCollision(food: Food) extends Message
  case class PlayerCollision(player: Player) extends Message
  case class CurrentScore(score: Double) extends Message

