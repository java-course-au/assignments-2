package ru.spbau.mit;

public class Point {

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Point substract(Point x, Point y) {
        return new Point(x.getX() - y.getX(), x.getY() - y.getY());
    }

    public static Point add(Point x, Point y) {
        return new Point(x.getX() + y.getX(), x.getY() + y.getY());
    }

    public static int compare(Point a, Point b) {
        if (a.x < b.x) {
            return -1;
        } else if (a.x > b.x) {
            return 1;
        } else {
            if (a.y < b.y) {
                return -1;
            } else if (a.y > b.y) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static int compareAngle(Point x, Point y) {
        if (x.getX() * y.getY() - x.getY() * y.getX() < 0) {
            return -1;
        } else if (x.getX() * y.getY() - x.getY() * y.getX() > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
