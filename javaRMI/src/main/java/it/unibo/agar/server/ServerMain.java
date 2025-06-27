package it.unibo.agar.server;

import it.unibo.agar.common.GameServer;
import it.unibo.agar.model.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ServerMain {
    public static void main(String[] args) {
        try {
            int width = 2000;
            int height = 2000;
            List<Player> players = List.of();
            List<Food> foods = GameInitializer.initialFoods(150, width, height);
            World world = new World(width, height, players, foods);

            RemoteGameServerImpl gameServer = new RemoteGameServerImpl(world);
            Registry registry = LocateRegistry.createRegistry(8080);
            registry.rebind("GameServer", gameServer);
            System.out.println("Server ready.");

            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(50);
                        gameServer.tick();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}