package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

class Canvas extends JPanel implements DefaultMouseListener {
    private static final int DELTA = 10;
    private static final int POINT_SIZE = 5;

    private final JPopupMenu popupMenu = new JPopupMenu();
    private List<Point> points;
    private List<Point> hull;
    private Point selectedPoint;

    Canvas() {
        addMouseListener(this);
        points = new ArrayList<>();
        hull = new ArrayList<>();
        popupMenu.add(buildPopupMenuItem());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                points.add(new Point(e.getX(), e.getY()));
                repaint();
                break;
            case MouseEvent.BUTTON3:
                selectedPoint = getPoint(e.getX(), e.getY());
                if (selectedPoint != null) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
                repaint();
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void calculate() {
        if (points.size() < 3) {
            return;
        }

        points.sort((o1, o2) -> {
            if (o1.getX() != o2.getX()) {
                return o1.getX() < o2.getX() ? -1 : 1;
            }
            if (o1.getY() != o2.getY()) {
                return o1.getY() < o2.getY() ? -1 : 1;
            }
            return 0;
        });

        Point firstPoint = points.get(0);
        points.remove(0);

        Point lastPoint = points.get(points.size() - 1);

        List<Point> upHull = new ArrayList<>();
        upHull.add(firstPoint);

        List<Point> downHull = new ArrayList<>();
        downHull.add(firstPoint);

        for (Point point: points) {
            if (point == lastPoint || getTurn(firstPoint, point, lastPoint) < 0) {
                while (upHull.size() > 1 && getLastTurn(upHull, point) >= 0) {
                    upHull.remove(upHull.size() - 1);
                }
                upHull.add(point);
            }

            if (point == lastPoint || getTurn(firstPoint, point, lastPoint) > 0) {
                while (downHull.size() > 1 && getLastTurn(downHull, point) <= 0) {
                    downHull.remove(downHull.size() - 1);
                }
                downHull.add(point);
            }
        }

        hull = upHull;
        for (int i = downHull.size() - 2; i > 0; --i) {
            hull.add(downHull.get(i));
        }

        repaint();
    }

    public void clear() {
        points.clear();
        hull.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);

        g.setColor(Color.red);
        for (int i = 0; i < hull.size(); ++i) {
            if (i == hull.size() - 1) {
                g.drawLine(hull.get(i).getX(), hull.get(i).getY(), hull.get(0).getX(), hull.get(0).getY());
            } else {
                g.drawLine(hull.get(i).getX(), hull.get(i).getY(), hull.get(i + 1).getX(), hull.get(i + 1).getY());
            }
        }

        for (Point point: points) {
            g.setColor(point == selectedPoint ? Color.green : Color.black);
            g.fillOval(point.getX(), point.getY(), POINT_SIZE, POINT_SIZE);
        }
    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click
        JMenuItem item = new JMenuItem("Delete");
        item.addActionListener((event) -> {
            if (selectedPoint != null) {
                points.remove(selectedPoint);
                if (hull.contains(selectedPoint)) {
                    hull.clear();
                }
                repaint();
            }
        });
        return item;
    }

    private Point getPoint(final int x, final int y) {
        for (Point point: points) {
            if (Math.abs(point.getX() - x) < DELTA && Math.abs(point.getY() - y) < DELTA) {
                return point;
            }
        }
        return null;
    }

    private int getTurn(final Point p1, final Point p2, final Point p3) {
        final int dx1 = p2.getX() - p1.getX();
        final int dx2 = p3.getX() - p1.getX();
        final int dy1 = p2.getY() - p1.getY();
        final int dy2 = p3.getY() - p1.getY();
        return dx1 * dy2 - dx2 * dy1;
    }

    private int getLastTurn(final List<Point> hull, final Point point) {
        return getTurn(hull.get(hull.size() - 2), hull.get(hull.size() - 1), point);
    }
}
