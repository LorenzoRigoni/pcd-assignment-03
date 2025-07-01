package it.unibo.agar.view

import akka.actor.typed.ActorRef
import it.unibo.agar.GameConf.*
import it.unibo.agar.PlayerProtocol.Move
import it.unibo.agar.{PlayerProtocol, ViewProtocol}
import it.unibo.agar.model.{MockGameStateManager, World}

import java.awt.Graphics2D
import javax.swing.JOptionPane
import javax.swing.plaf.ViewportUI
import scala.swing.*

class LocalView(manager: ActorRef[ViewProtocol.ViewMessage], playerId: String, playerActor: ActorRef[PlayerProtocol.PlayerMessage]) extends MainFrame:
  private var world: World = World(worldWidth, worldHeight, Seq.empty, Seq.empty)

  title = s"Agar.io - Local View ($playerId)"
  preferredSize = new Dimension(400, 400)

  private val panel = new Panel:
    listenTo(keys, mouse.moves)
    focusable = true
    requestFocusInWindow()

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      val playerOpt = world.players.find(_.id == playerId)
      val (offsetX, offsetY) = playerOpt
        .map(p => (p.x - size.width / 2.0, p.y - size.height / 2.0))
        .getOrElse((0.0, 0.0))
      AgarViewUtils.drawWorld(g, world, offsetX, offsetY)

    reactions += {
      case e: event.MouseMoved =>
        val mousePos = e.point
        val dx = (mousePos.x - size.width / 2) * 0.01
        val dy = (mousePos.y - size.height / 2) * 0.01
        playerActor ! Move(dx, dy)
    }

  contents = panel

  def updateWorld(newWorld: World): Unit =
    this.world = newWorld
    panel.repaint()

  def displayVictory(playerId: String, score: Double): Unit =
    JOptionPane.showMessageDialog(null, playerId + " wins the game! Score: " + score, "Game over", JOptionPane.INFORMATION_MESSAGE)