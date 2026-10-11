package entity;

public abstract class Entity {
    protected double x;
    protected double y;
    protected double radius;
    protected double hp;
    protected double maxHp;
    protected boolean alive = true;

    protected Entity(double x, double y, double radius, double hp) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.hp = hp;
        maxHp = hp;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public double getHp() {
        return hp;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public boolean isAlive() {
        return alive;
    }

    public void damage(double amount) {
        if (amount <= 0.0 || !alive) {
            return;
        }
        hp -= amount;
        if (hp <= 0.0) {
            hp = 0.0;
            alive = false;
        }
    }
}
