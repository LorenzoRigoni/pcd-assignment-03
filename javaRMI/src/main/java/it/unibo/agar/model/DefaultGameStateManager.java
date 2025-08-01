package it.unibo.agar.model;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultGameStateManager implements GameStateManager {
    private static final double PLAYER_SPEED = 2.0;
    private static final int MAX_FOOD_ITEMS = 150;
    private static final Random random = new Random();
    private static final double SCORE_TO_WIN = 1000.0;
    private World world;
    private final Map<String, Position> playerDirections;
    private boolean isGameOver;
    private String winnerName;
    private double winnerScore;

    public synchronized void addPlayer(Player player) {
        List<Player> newPlayers = new ArrayList<>(world.getPlayers());
        newPlayers.add(player);
        this.world = new World(world.getWidth(), world.getHeight(), newPlayers, world.getFoods());
        playerDirections.put(player.getId(), Position.ZERO);
    }

    public synchronized void removePlayers(List<Player> playersToRemove) {
        List<String> ids = playersToRemove.stream().map(Player::getId).toList();
        List<Player> newPlayers = world.getPlayers().stream()
            .filter(p -> !ids.contains(p.getId()))
            .toList();
        this.world = new World(world.getWidth(), world.getHeight(), newPlayers, world.getFoods());
    }

    public DefaultGameStateManager(final World initialWorld) {
        this.world = initialWorld;
        this.playerDirections = new HashMap<>();
        this.world.getPlayers().forEach(p -> playerDirections.put(p.getId(), Position.ZERO));
        this.isGameOver = false;
    }

    public synchronized World getWorld() {
    return this.world;
}

    public synchronized void tick() {
        if (!this.isGameOver) {
            this.world = handleEating(moveAllPlayers(this.world));
            cleanupPlayerDirections();
        }
    }

    @Override
    public boolean isGameOver() {
        return this.isGameOver;
    }

    @Override
    public String getWinnerName() {
        return this.winnerName;
    }

    @Override
    public double getWinnerScore() {
        return this.winnerScore;
    }

    @Override
    public void setPlayerDirection(final String playerId, final double dx, final double dy) {
        // Ensure player exists before setting direction
        if (world.getPlayerById(playerId).isPresent()) {
            this.playerDirections.put(playerId, Position.of(dx, dy));
        }
    }

    private World moveAllPlayers(final World currentWorld) {
        final List<Player> updatedPlayers = currentWorld.getPlayers().stream()
            .map(player -> {
                Position direction = playerDirections.getOrDefault(player.getId(), Position.ZERO);
                final double newX = player.getX() + direction.x() * PLAYER_SPEED;
                final double newY = player.getY() + direction.y() * PLAYER_SPEED;
                return player.moveTo(newX, newY);
            })
            .collect(Collectors.toList());

        return new World(currentWorld.getWidth(), currentWorld.getHeight(), updatedPlayers, currentWorld.getFoods());
    }

    private World handleEating(final World currentWorld) {
        final List<Player> updatedPlayers = currentWorld.getPlayers().stream()
                .map(player -> growPlayer(currentWorld, player))
                .toList();

        final List<Food> foodsToRemove = currentWorld.getPlayers().stream()
                .flatMap(player -> eatenFoods(currentWorld, player).stream())
                .distinct()
                .toList();

        final List<Player> playersToRemove = currentWorld.getPlayers().stream()
                .flatMap(player -> eatenPlayers(currentWorld, player).stream())
                .distinct()
                .toList();

        final Set<Player> potentialWinners = updatedPlayers.stream().filter(p -> p.getMass() >= SCORE_TO_WIN)
                .collect(Collectors.toSet());

        if (!potentialWinners.isEmpty()) {
            this.isGameOver = true;
            this.winnerName = potentialWinners.iterator().next().getName();
            this.winnerScore = potentialWinners.iterator().next().getMass();
        }

        return new World(currentWorld.getWidth(), currentWorld.getHeight(), updatedPlayers, currentWorld.getFoods())
                .removeFoods(foodsToRemove)
                .removePlayers(playersToRemove);
    }

    private Player growPlayer(final World world, final Player player) {
        final Player afterFood = eatenFoods(world, player).stream()
                .reduce(player, Player::grow, (p1, p2) -> p1);

        return eatenPlayers(world, afterFood).stream()
                .reduce(afterFood, Player::grow, (p1, p2) -> p1);
    }

    private List<Food> eatenFoods(final World world, final Player player) {
        return world.getFoods().stream()
                .filter(food -> EatingManager.canEatFood(player, food))
                .toList();
    }

    private List<Player> eatenPlayers(final World world, final Player player) {
        return world.getPlayersExcludingSelf(player).stream()
                .filter(other -> EatingManager.canEatPlayer(player, other))
                .toList();
    }

    private void cleanupPlayerDirections() {
        List<String> currentPlayerIds = this.world.getPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toList());

        this.playerDirections.keySet().retainAll(currentPlayerIds);
        this.world.getPlayers().forEach(p ->
                playerDirections.putIfAbsent(p.getId(), Position.ZERO));
    }
}
