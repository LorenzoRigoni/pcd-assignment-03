package it.unibo.agar.client;

import it.unibo.agar.common.GameServer;
import it.unibo.agar.model.Player;
import it.unibo.agar.view.LocalView;

import java.rmi.registry.*;

import javax.swing.*;

public class ClientMain {
    private static final int REFRESH_RATE_MS = 50; // ~20 FPS

    public static void main(String[] args) {
        try {
            final Registry registry = LocateRegistry.getRegistry("localhost", 8080);
            final GameServer gameServer = (GameServer) registry.lookup("GameServer");

            String playerName;

            do {
                playerName = JOptionPane.showInputDialog(null,
                        "Enter your name:",
                        "Player ID",
                        JOptionPane.QUESTION_MESSAGE);
            } while(playerName.isEmpty());

            final Player player = gameServer.joinGame(playerName);
            System.out.println("Joined game as: " + player.getId());

            final RemoteGameStateManagerProxy gameStateManager = new RemoteGameStateManagerProxy(gameServer, player.getId());
            final LocalView view = new LocalView(gameStateManager, player.getId(), playerName);
            view.setVisible(true);

            // Game loop for continuous updates
            final Timer timer = new Timer(REFRESH_RATE_MS, e -> {
                view.repaintView();
            });
            timer.start();

            // Cleanup on window close
            view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            view.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    try {
                        gameServer.leaveGame(player.getId());
                        timer.stop();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}