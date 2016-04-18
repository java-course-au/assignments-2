package ru.spbau.mit;

import java.util.Comparator;

public class Point {

    public static final Comparator<Point> LEFT_DOWN_COMPARATOR = (o1, o2) -> {
        if (o1.getX() == o2.getX()) {
            return Integer.compare(o1.getY(), o2.getY());
        }
        return Integer.compare(o1.getX(), o2.getX());
    };
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Comparator<Point> getAngleComparator(Point start) {
        return (o1, o2) -> {
            o1 = o1.sub(start);
            o2 = o2.sub(start);
            double angle1 = o1.getAngle();
            double angle2 = o2.getAngle();
            if (angle1 == angle2) {
                return Double.compare(o1.getDist(), o2.getDist());
            }
            return Double.compare(angle1, angle2);
        };
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getAngle() {
        return Math.atan2(y, x);
    }

    public double getDist() {
        return Math.sqrt(x * x + y * y);
    }

    public Point sub(Point other) {
        return new Point(x - other.getX(), y - other.getY());
    }

    public int cross(Point p1, Point p2) {
        Point b = p2.sub(p1);
        Point c = sub(p1);
        return b.x * c.y - c.x * b.y;
    }
}
