package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

class Canvas extends JPanel implements DefaultMouseListener {

    private static final double DELETE_MIN_DIST = 20;
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final ConvexHull convexHull = new ConvexHull();
    private Point pointToDelete;

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point eventPoint = new Point(e.getX(), e.getY());
        double minDist = 1e9;
        Point nearestPoint = null;

        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                convexHull.addPoint(eventPoint);
                break;

            case MouseEvent.BUTTON3:
                for (Point p : convexHull.getPoints()) {
                    if (eventPoint.dist(p) < minDist) {
                        minDist = eventPoint.dist(p);
                        nearestPoint = p;
                    }
                }
                if (minDist < DELETE_MIN_DIST) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    pointToDelete = nearestPoint;
                }
                break;

            default:
                throw new UnsupportedOperationException();
        }
        repaint();
    }

    public void calculate() {
        convexHull.rebuild();
        repaint();
    }

    public void clear() {
        convexHull.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method
        Graphics2D g2 = (Graphics2D) g;

        g2.clearRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.BLACK);

        List<Point> hull = convexHull.getHull();

        g2.setStroke(new BasicStroke(2));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < hull.size(); i++) {
            Point p1 = hull.get(i);
            Point p2 = hull.get((i + 1) % hull.size());
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }

        for (Point p : convexHull.getPoints()) {
            g2.fillOval(p.getX() - 5, p.getY() - 5, 10, 10);
        }
    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click
        JMenuItem deleteItem = new JMenuItem("delete");
        deleteItem.addActionListener(e -> {
            convexHull.getPoints().remove(pointToDelete);
            repaint();
        });
        return deleteItem;
    }
}
