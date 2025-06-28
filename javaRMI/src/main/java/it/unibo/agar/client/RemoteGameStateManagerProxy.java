package it.unibo.agar.client;

import it.unibo.agar.common.GameServer;
import it.unibo.agar.model.*;
import java.rmi.RemoteException;
import java.util.List;

public class RemoteGameStateManagerProxy implements GameStateManager {
    private final GameServer gameServer;
    private final String playerId;
    private World cachedWorld;

    public RemoteGameStateManagerProxy(GameServer gameServer, String playerId) {
        this.gameServer = gameServer;
        this.playerId = playerId;
    }

    @Override
    public World getWorld() {
        try {
            cachedWorld = gameServer.getWorld();
        } catch (RemoteException e) {
            System.err.println("Network error, using cached world");
            if (cachedWorld == null) {
                // Return empty world if no cache exists
                return new World(2000, 2000, List.of(), List.of());
            }
        }
        return cachedWorld;
    }

    @Override
    public void setPlayerDirection(String playerId, double dx, double dy) {
        try {
            gameServer.setDirection(playerId, dx, dy);
        } catch (RemoteException e) {
            System.err.println("Failed to update direction: " + e.getMessage());
        }
    }

    @Override
    public void tick() {
        // Client doesn't handle ticks
    }

    @Override
    public boolean isGameOver() {
        return false;
    }

    @Override
    public String getWinnerName() {
        return "";
    }

    @Override
    public double getWinnerScore() {
        return 0;
    }
}