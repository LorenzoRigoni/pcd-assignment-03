package it.unibo.agar.server;

import it.unibo.agar.common.GameServer;
import it.unibo.agar.model.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;

public class RemoteGameServerImpl extends UnicastRemoteObject implements GameServer {
    private final GameStateManager gameStateManager;
    private final ScheduledExecutorService executor;
    private final Set<String> connectedPlayers = ConcurrentHashMap.newKeySet();

    public RemoteGameServerImpl(World initialWorld) throws RemoteException {
        super();
        this.gameStateManager = new DefaultGameStateManager(initialWorld);
        this.executor = Executors.newScheduledThreadPool(2);
        
        // Start game loop
        executor.scheduleAtFixedRate(() -> {
            try {
                synchronized (this) {
                    gameStateManager.tick();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 50, TimeUnit.MILLISECONDS); // 20 updates/second
    }

    @Override
    public synchronized Player joinGame(String playerName) throws RemoteException {
        String id = "p" + System.currentTimeMillis();
        Player player = new Player(id,
            playerName,
            Math.random() * gameStateManager.getWorld().getWidth(), 
            Math.random() * gameStateManager.getWorld().getHeight(), 
            120.0);

        ((DefaultGameStateManager) gameStateManager).addPlayer(player);
        connectedPlayers.add(id);
        System.out.println("Player joined: " + playerName);
        return player;
    }

    @Override
    public synchronized void leaveGame(String playerId) throws RemoteException {
        Optional<Player> playerOpt = gameStateManager.getWorld().getPlayerById(playerId);
        if (playerOpt.isPresent()) {
            ((DefaultGameStateManager) gameStateManager).removePlayers(List.of(playerOpt.get()));
            connectedPlayers.remove(playerId);
            System.out.println("Player left: " + playerId);
        }
    }

    @Override
    public synchronized void setDirection(String playerId, double dx, double dy) throws RemoteException {
        if (connectedPlayers.contains(playerId)) {
            gameStateManager.setPlayerDirection(playerId, dx, dy);
        }
    }

    @Override
    public synchronized World getWorld() throws RemoteException {
        return gameStateManager.getWorld();
    }

    @Override
    public GameStateManager getGameStateManager() throws RemoteException {
        return this.gameStateManager;
    }

    public void tick() {
        gameStateManager.tick();
    }
}