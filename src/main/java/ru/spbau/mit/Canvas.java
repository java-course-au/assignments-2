package ru.spbau.mit;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class Canvas extends JPanel implements DefaultMouseListener {
    private static final int RADIUS = 10;
    private final MyPopupMenu popupMenu = new MyPopupMenu();

    private final List<Point> points = new ArrayList<>();
    private List<Point> convexHull = Collections.emptyList();

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
        this.setComponentPopupMenu(popupMenu);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                if (popupMenu.isVisible()) {
                    return;
                }
                points.add(new Point(e.getPoint()));
                repaint();
                break;
            default:
                break;
        }
    }

    public void calculate() {
        if (points.size() <= 1) {
            convexHull = Collections.emptyList();
            return;
        }
        int basePointId = 0;
        for (int i = 1; i != points.size(); i++) {
            Point basePoint = points.get(basePointId);
            Point altPoint = points.get(i);
            if (Point.BASE_POINT_COMPARATOR.compare(basePoint, altPoint) != 1) {
                continue;
            }
            basePointId = i;
        }
        Point basePoint = points.get(basePointId);

        List<Point> candidates = new ArrayList<>(points.size());
        for (int i = 0; i != points.size(); i++) {
            if (i == basePointId) {
                continue;
            }
            candidates.add(points.get(i).subtract(basePoint));
        }
        candidates.sort(Point.ANGLE_COMPARATOR);
        candidates.add(0, Point.ZERO);

        convexHull = new ArrayList<>();
        for (Point candidate: candidates) {
            while (convexHull.size() >= 2) {
                Point b = convexHull.get(convexHull.size() - 1);
                Point c = convexHull.get(convexHull.size() - 2);
                Point ba = candidate.subtract(b);
                Point cb = b.subtract(c);
                if (cb.cross(ba) > 0) {
                    break;
                }
                convexHull.remove(convexHull.size() - 1);
            }
            convexHull.add(candidate);
        }
        convexHull.add(Point.ZERO);

        convexHull = convexHull
                .stream()
                .map(point -> point.add(basePoint))
                .collect(Collectors.toList());
        repaint();
    }

    public void clear() {
        points.clear();
        convexHull.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method

        g.clearRect(0, 0, getWidth(), getHeight());
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.CYAN);
        for (Point point: points) {
            g.fillOval(point.getX() - RADIUS + 1, point.getY() - RADIUS + 1, 2 * RADIUS - 1, 2 * RADIUS - 1);
        }
        g.setColor(Color.BLUE);
        for (Point point: points) {
            g.drawOval(point.getX() - RADIUS, point.getY() - RADIUS, 2 * RADIUS, 2 * RADIUS);
        }
        for (int i = 0; i < convexHull.size() - 1; i++) {
            Point a = convexHull.get(i);
            Point b = convexHull.get(i + 1);
            Point offset = b.subtract(a).norm().setLength(RADIUS + 1);
            Point a1 = a.add(offset);
            Point b1 = b.add(offset);
            g.drawLine(a1.getX(), a1.getY(), b1.getX(), b1.getY());
        }

    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click
        JMenuItem remove = new JMenuItem("Remove point");
        remove.addActionListener(e -> {
            Point p = null;
            Point clickPosition = popupMenu.getLastPoint();
            for (Point point: points) {
                if (p == null || point.distance(clickPosition) < p.distance(clickPosition)) {
                    p = point;
                }
            }
            if (p != null) {
                points.remove(p);
                repaint();
            }
        });
        return remove;
    }
}
