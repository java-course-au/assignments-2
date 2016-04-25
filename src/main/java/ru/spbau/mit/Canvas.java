package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

class Canvas extends JPanel implements DefaultMouseListener {
    private static final int POINT_RADIUS = 5;

    private List<Point> points = new ArrayList<>();
    private List<Point> pointsInHull = new ArrayList<>();
    private Point mousePoint;

    private final JPopupMenu popupMenu = new JPopupMenu();

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
        points = new ArrayList<>();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                points.add(new Point(e.getX(), e.getY()));
                repaint();
                break;
            case MouseEvent.BUTTON3:
                mousePoint = new Point(e.getX(), e.getY());
                popupMenu.show(this, e.getX(), e.getY());
            default:
        }
    }

    private Point subtract(Point a, Point b) {
        return new Point(a.getX() - b.getX(), a.getY() - b.getY());
    }

    private int getCrossProduct(Point a, Point b) {
        return a.getX() * b.getY() - a.getY() * b.getX();
    }

    public void calculate() {
        pointsInHull = new ArrayList<>();

        if (points.size() < 2) {
            repaint();
            return;
        }

        Point startPoint = points.stream().min(Comparator.comparingInt(Point::getX)
                .thenComparingInt(Point::getY)).get();

        points.sort((a, b) -> {
            if (a.getX() == b.getX() && a.getY() == b.getY()) {
                return 0;
            }
            int crossProduct = getCrossProduct(subtract(a, startPoint), subtract(b, startPoint));
            if (crossProduct == 0) {
                return getSquareOfDistance(a, startPoint) - getSquareOfDistance(b, startPoint);
            } else {
                return crossProduct;
            }
        });

        points.add(startPoint);

        pointsInHull.add(points.get(0));
        pointsInHull.add(points.get(1));

        for (int i = 2; i < points.size(); i++) {
            Point newPoint = points.get(i);
            while (pointsInHull.size() > 2 && getCrossProduct(
                    subtract(newPoint, pointsInHull.get(pointsInHull.size() - 1)),
                    subtract(pointsInHull.get(pointsInHull.size() - 2),
                            pointsInHull.get(pointsInHull.size() - 1))) > 0) {
                pointsInHull.remove(pointsInHull.size() - 1);
            }
            pointsInHull.add(newPoint);
        }

        points.remove(points.size() - 1);

        repaint();
    }

    public void clear() {
        points = new ArrayList<>();
        pointsInHull = new ArrayList<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);

        for (Point point : points) {
            g.drawOval(point.getX() - POINT_RADIUS, point.getY() - POINT_RADIUS, POINT_RADIUS, POINT_RADIUS);
        }

        for (int i = 0; i < pointsInHull.size() - 1; i++) {
            g.drawLine(pointsInHull.get(i).getX(), pointsInHull.get(i).getY(),
                    pointsInHull.get(i + 1).getX(), pointsInHull.get(i + 1).getY());
        }
    }

    private int getSquareOfDistance(Point p1, Point p2) {
        return (p1.getX() - p2.getX()) * (p1.getX() - p2.getX())
                + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY());
    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click

        JMenuItem menuItem = new JMenuItem("Remove point");
        menuItem.addActionListener((e) -> {
            for (Point point : points) {
                if (getSquareOfDistance(point, mousePoint) < POINT_RADIUS * POINT_RADIUS) {
                    points.remove(point);
                    pointsInHull = new ArrayList<>();
                    repaint();
                    break;
                }
            }
            repaint();
        });
        popupMenu.add(menuItem);

        return menuItem;
    }
}
