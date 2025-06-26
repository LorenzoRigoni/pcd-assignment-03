package it.unibo.agar.client;

import it.unibo.agar.model.World;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void updateWorld(World world) throws RemoteException;
}