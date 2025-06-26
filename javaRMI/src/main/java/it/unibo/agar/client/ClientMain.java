package it.unibo.agar.client;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        GameClient client = new GameClient("localhost");
        // Example: Move towards top-right
        client.move(0.5, -0.5);
    }
}