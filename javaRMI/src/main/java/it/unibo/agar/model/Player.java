package it.unibo.agar.model;

public class Player extends AbstractEntity {
    private final String name;

    public Player(final String id, final String name, final double x, final double y, final double mass) {
        super(id, x, y, mass);
        this.name = name;
    }


    public Player grow(Entity entity) {
        return new Player(getId(), this.name, getX(), getY(), getMass() + entity.getMass());
    }

    public Player moveTo(double newX, double newY) {
        return new Player(getId(), this.name, newX, newY, getMass());
    }

    public String getName() {
        return this.name;
    }
}
