package ru.spbau.mit;

public class Point implements Comparable<Point> {

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDistanceSquare() {
        return this.x * this.x + this.y * this.y;
    }

    public Point subtract(Point p) {
        return new Point(x - p.getX(), y - p.getY());
    }


    public Point add(Point p) {
        return new Point(x + p.getX(), y + p.getY());
    }

    @Override
    public int compareTo(Point o) {
        if (x != o.x) {
            return Integer.compare(x, o.x);
        }
        return Integer.compare(y, o.y);
    }
}
