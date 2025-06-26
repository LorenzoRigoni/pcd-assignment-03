package it.unibo.agar.client;

import it.unibo.agar.model.*;
import it.unibo.agar.server.GameServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;

public class GameClient extends UnicastRemoteObject implements ClientCallback {
    private final String playerId;
    private World currentWorld;

    public GameClient(String serverAddress) throws Exception {
        Registry registry = LocateRegistry.getRegistry(serverAddress);
        GameServer server = (GameServer) registry.lookup("GameServer");
        this.playerId = server.joinGame(this);
        System.out.println("Joined as: " + playerId);
    }

    @Override
    public void updateWorld(World world) {
        this.currentWorld = world;
        render();
    }

    private void render() {
        Optional<Player> self = currentWorld.getPlayerById(playerId);
        self.ifPresentOrElse(
            player -> System.out.printf("Pos: (%.1f,%.1f) Mass: %.1f%n", 
                player.getX(), player.getY(), player.getMass()),
            () -> System.out.println("You died!")
        );
    }

    public void move(double dx, double dy) {
        try {
            Registry registry = LocateRegistry.getRegistry();
            GameServer server = (GameServer) registry.lookup("GameServer");
            server.setPlayerDirection(playerId, dx, dy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}