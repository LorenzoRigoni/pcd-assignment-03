import it.unibo.agar.controller.{GameClient, GameServer}

import javax.swing.JOptionPane

object Main:
  def main(args: Array[String]): Unit =
    if args.isEmpty then
      println("Starting server and client with default player...")

      // Avvia server in un thread separato
      val serverThread = new Thread(() => GameServer.main(Array.empty))
      serverThread.setDaemon(true)
      serverThread.start()

      // Attendi che il server sia pronto (es. 2 secondi)
      Thread.sleep(2000)

      val playerId = JOptionPane.showInputDialog(null, "Insert your name:", "Player ID", JOptionPane.QUESTION_MESSAGE);
      val finalId = Option(playerId).filter(_.nonEmpty).getOrElse("Player1")

      // Avvia client con playerId di default
      GameClient.main(Array(finalId))
    else
      args(0).toLowerCase match
        case "server" =>
          GameServer.main(Array.empty)
        case "client" =>
          val playerId = if args.length > 1 then args(1) else
            val input = JOptionPane.showInputDialog(null, "Insert your name:", "Player ID", JOptionPane.QUESTION_MESSAGE)
            Option(input).filter(_.nonEmpty).getOrElse("Player1")
          GameClient.main(Array(playerId))
        case _ =>
          println(s"Unknown argument: ${args(0)}")
          System.exit(1)