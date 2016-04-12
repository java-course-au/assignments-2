package ru.spbau.mit;

import java.util.Comparator;

@SuppressWarnings("SuspiciousNameCombination")
public class Point {
    public static final Comparator<Point> BASE_POINT_COMPARATOR = (Point o1, Point o2) -> {
        if (o1.y != o2.y) {
            return Integer.compare(o1.y, o2.y);
        }
        return Integer.compare(o1.x, o2.x);
    };

    public static final Comparator<Point> ANGLE_COMPARATOR = (Point o1, Point o2) -> {
        if (o1.cross(o2) != 0) {
            return Integer.compare(0, o1.cross(o2));
        }
        return Integer.compare(o1.lengthSqr(), o2.lengthSqr());
    };

    public static final Point ZERO = new Point(0, 0);

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(java.awt.Point point) {
        this(point.x, point.y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point add(Point that) {
        return new Point(this.x + that.x, this.y + that.y);
    }

    public Point subtract(Point that) {
        return new Point(this.x - that.x, this.y - that.y);
    }

    public int dot(Point that) {
        return this.x * that.x + this.y * that.y;
    }

    public int cross(Point that) {
        return this.x * that.y - this.y * that.x;
    }

    public int distance(Point that) {
        Point sub = subtract(that);
        return sub.dot(sub);
    }

    public Point norm() {
        //noinspection SuspiciousNameCombination
        return new Point(y, -x);
    }

    public int lengthSqr() {
        return this.dot(this);
    }

    public Point setLength(double newLength) {
        double length = Math.sqrt(lengthSqr());
        return new Point((int) (x / length * newLength), (int) (y / length * newLength));
    }
}
