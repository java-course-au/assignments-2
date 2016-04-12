package ru.spbau.mit;

public class Point {

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point add(Point p) {
        return new Point(x + p.x, y + p.y);
    }

    public Point subtract(Point p) {
        return new Point(x - p.x, y - p.y);
    }

    public boolean isToTheLeft(Point p) {
        return x * p.y - y * p.x < 0;
    }

    private double sqr(double a) {
        return a * a;
    }

    public double dist(Point p) {
        return subtract(p).len();
    }

    public double len() {
        return Math.sqrt(sqr(x) + sqr(y));
    }
}
