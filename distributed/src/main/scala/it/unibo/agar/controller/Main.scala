import it.unibo.agar.controller.{GameClient, GameServer}

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

      // Avvia client con playerId di default
      GameClient.main(Array("Player1"))
    else
      args(0).toLowerCase match
        case "server" =>
          GameServer.main(Array.empty)
        case "client" =>
          val playerId = if args.length > 1 then args(1) else "Player1"
          GameClient.main(Array(playerId))
        case _ =>
          println(s"Unknown argument: ${args(0)}")
          System.exit(1)