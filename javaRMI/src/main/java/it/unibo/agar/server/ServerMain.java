package it.unibo.agar.server;

import it.unibo.agar.model.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        World initialWorld = new World(1000, 700, List.of(), 
            GameInitializer.initialFoods(100, 1000, 700));
        GameServer server = new GameServerImpl(initialWorld);
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("GameServer", server);
        System.out.println("Server ready");
    }
}