package it.unibo.agar.common;

import it.unibo.agar.model.Player;
import it.unibo.agar.model.World;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameServer extends Remote {
    Player joinGame(String playerName) throws RemoteException;
    void leaveGame(String playerId) throws RemoteException;
    void setDirection(String playerId, double dx, double dy) throws RemoteException;
    World getWorld() throws RemoteException;
}