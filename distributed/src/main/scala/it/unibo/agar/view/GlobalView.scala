package it.unibo.agar.view

import it.unibo.agar.GameConf.{worldHeight, worldWidth}
import it.unibo.agar.model.{MockGameStateManager, World}

import java.awt.Color
import java.awt.Graphics2D
import scala.swing.*

class GlobalView extends MainFrame:
  private var world: World = World(worldWidth, worldHeight, Seq.empty, Seq.empty)
  title = "Agar.io - Global View"
  preferredSize = new Dimension(800, 800)

  private val panel = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      AgarViewUtils.drawWorld(g, world)

  contents = panel

  def updateWorld(newWorld: World): Unit =
    world = newWorld
    panel.repaint()