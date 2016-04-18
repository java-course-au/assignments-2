package ru.spbau.mit;

public class Point implements Comparable<Point> {

    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        x = point.x;
        y = point.y;
    }

    public Point(Point a, Point b) {
        x = b.x - a.x;
        y = b.y - a.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int compareTo(Point b) {
        return x == b.x ? y - b.y : x - b.x;
    }

    public int crossProduct(Point b) {
        return x * b.y - y * b.x;
    }

    public int length2() {
        return x * x + y * y;
    }

    public int distance2(Point point) {
        return (new Point(this, point)).length2();
    }

    public int distance2(int x, int y) {
        return (new Point(this, new Point(x, y))).length2();
    }

    public void add(Point point) {
        x += point.x;
        y += point.y;
    }

    public void sub(Point point) {
        x -= point.x;
        y -= point.y;
    }
}
