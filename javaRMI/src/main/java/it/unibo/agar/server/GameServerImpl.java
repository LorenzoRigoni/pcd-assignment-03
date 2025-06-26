package it.unibo.agar.server;

import it.unibo.agar.model.*;
import it.unibo.agar.client.ClientCallback;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;

public class GameServerImpl extends UnicastRemoteObject implements GameServer {
    private final DefaultGameStateManager gameStateManager;
    private final ScheduledExecutorService gameLoop;
    private final Map<String, ClientCallback> clients = new ConcurrentHashMap<>();
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();

    public GameServerImpl(World initialWorld) throws RemoteException {
        this.gameStateManager = new DefaultGameStateManager(initialWorld);
        this.gameLoop = Executors.newSingleThreadScheduledExecutor();
        this.gameLoop.scheduleAtFixedRate(this::gameTick, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void gameTick() {
        synchronized (gameStateManager) {
            gameStateManager.tick();
            World currentWorld = gameStateManager.getWorld();
            removeInactivePlayers();
            clients.forEach((id, callback) -> {
                try {
                    callback.updateWorld(currentWorld);
                } catch (RemoteException e) {
                    System.err.println("Client disconnected: " + id);
                    clients.remove(id);
                }
            });
        }
    }

    private void removeInactivePlayers() {
        long now = System.currentTimeMillis();
        List<Player> toRemove = new ArrayList<>();
        gameStateManager.getWorld().getPlayers().forEach(player -> {
            if (now - lastHeartbeat.getOrDefault(player.getId(), 0L) > 5000) {
                toRemove.add(player);
            }
        });
        gameStateManager.removePlayers(toRemove);
    }

    @Override
    public String joinGame(ClientCallback callback) throws RemoteException {
        String playerId = "player_" + UUID.randomUUID();
        Player player = new Player(playerId, 
            ThreadLocalRandom.current().nextInt(gameStateManager.getWorld().getWidth()),
            ThreadLocalRandom.current().nextInt(gameStateManager.getWorld().getHeight()),
            120.0
        );
        synchronized (gameStateManager) {
            gameStateManager.addPlayer(player);
        }
        clients.put(playerId, callback);
        lastHeartbeat.put(playerId, System.currentTimeMillis());
        return playerId;
    }

    @Override
    public void setPlayerDirection(String playerId, double dx, double dy) throws RemoteException {
        lastHeartbeat.put(playerId, System.currentTimeMillis());
        gameStateManager.setPlayerDirection(playerId, dx, dy);
    }

    @Override
    public World getCurrentWorld() throws RemoteException {
        return gameStateManager.getWorld();
    }
}