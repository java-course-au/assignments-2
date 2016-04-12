package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Сева on 12.04.2016.
 */
public class ConvexHull {
    private List<Point> points = new ArrayList<>();
    private List<Point> hull = new ArrayList<>();

    void addPoint(Point p) {
        points.add(p);
    }

    void rebuild() {
        if (points.size() < 2) {
            return;
        }

        Point pivot = points.get(0);
        for (Point p : points) {
            if (p.getX() < pivot.getX() || (p.getX() == pivot.getX() && p.getY() < pivot.getY())) {
                pivot = p;
            }
        }
        final Point finalPivot = pivot;
        List<Point> sortedPoints = new ArrayList<>();
        points.forEach(p -> sortedPoints.add(p.subtract(finalPivot)));

        Collections.sort(sortedPoints, (p1, p2) -> {
            if (p2.isToTheLeft(p1)) {
                return -1;
            }
            if (p1.isToTheLeft(p2)) {
                return 1;
            }
            return Double.compare(p1.len(), p2.len());
        });

        List<Point> convexHull = new ArrayList<>();
        for (Point p : sortedPoints) {
            while (convexHull.size() > 2) {
                int size = convexHull.size();
                Point p2 = convexHull.get(size - 2);
                Point p1 = convexHull.get(size - 1);
                if (p.subtract(p1).isToTheLeft(p1.subtract(p2))) {
                    break;
                }
                convexHull.remove(size - 1);
            }
            convexHull.add(p);
        }
        hull.clear();
        convexHull.forEach(p -> hull.add(p.add(finalPivot)));
    }

    List<Point> getHull() {
        return hull;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void clear() {
        points.clear();
        hull.clear();
    }
}
