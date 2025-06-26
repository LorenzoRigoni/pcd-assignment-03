package it.unibo.agar.server;

import it.unibo.agar.model.World;
import it.unibo.agar.client.ClientCallback;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameServer extends Remote {
    String joinGame(ClientCallback callback) throws RemoteException;
    void setPlayerDirection(String playerId, double dx, double dy) throws RemoteException;
    World getCurrentWorld() throws RemoteException;
}