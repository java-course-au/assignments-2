package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

class Canvas extends JPanel implements DefaultMouseListener {

    private static final int R = 20;
    private final ArrayList<Point> points = new ArrayList<>();
    private final ArrayList<Point> upper = new ArrayList<>();
    private final ArrayList<Point> lower = new ArrayList<>();
    private final JPopupMenu popupMenu = new JPopupMenu();

    private Point clickLocation = new Point(0, 0);

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                Point point = new Point(e.getX(), e.getY());
                points.add(point);
                repaint();
                break;
            case MouseEvent.BUTTON3:
                clickLocation = new Point(e.getX(), e.getY());
                popupMenu.show(this, e.getX(), e.getY());
                break;
            default:
                break;
        }
    }

    public void calculate() {
        if (points.size() == 0) {
            return;
        }

        Collections.sort(points, (a, b) -> {
            if (a.getX() != b.getX()) {
                return a.getX() - b.getX();
            }
            if (a.getY() < b.getY()) {
                return a.getY() - b.getY();
            }
            return 0;
        });
        Point a = points.get(0);
        Point b = points.get(points.size() - 1);
        upper.clear();
        lower.clear();
        upper.add(a);
        lower.add(a);
        for (Point p : points) {
            if (cw(a, p, b) || p.equals(b)) {
                while (upper.size() >= 2 && ccw(upper.get(upper.size() - 2), upper.get(upper.size() - 1), p)) {
                    upper.remove(upper.size() - 1);
                }
                upper.add(p);
            }
            if (ccw(a, p, b) || p.equals(b)) {
                while (lower.size() >= 2 && cw(lower.get(lower.size() - 2), lower.get(lower.size() - 1), p)) {
                    lower.remove(lower.size() - 1);
                }
                lower.add(p);
            }
        }

        // draw the lines and take a look
        repaint();
    }

    public void clear() {
        points.clear();
        upper.clear();
        lower.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);

        for (Point point : points) {
            g.fillOval(point.getX(), point.getY(), R, R);
        }

        for (int i = 1; i < upper.size(); i++) {
            g.drawLine(upper.get(i - 1).getX(), upper.get(i - 1).getY(),
                    upper.get(i).getX(), upper.get(i).getY());
        }
        for (int i = 1; i < lower.size(); i++) {
            g.drawLine(lower.get(i - 1).getX(), lower.get(i - 1).getY(),
                    lower.get(i).getX(), lower.get(i).getY());
        }
    }

    private int getDist(int x1, int y1, int x2, int y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    private boolean cw(Point a, Point b, Point c) {
        return
                a.getX()
                        * (b.getY() - c.getY()) + b.getX()
                        * (c.getY() - a.getY()) + c.getX()
                        * (a.getY() - b.getY())
                        < 0;
    }

    private boolean ccw(Point a, Point b, Point c) {
        return
                a.getX()
                        * (b.getY() - c.getY()) + b.getX()
                        * (c.getY() - a.getY()) + c.getX()
                        * (a.getY() - b.getY())
                        > 0;
    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click

        JMenuItem popup = new JMenuItem("Remove point");
        popup.addActionListener(e -> {
            for (Iterator<Point> pointIt = points.iterator(); pointIt.hasNext();) {
                Point point = pointIt.next();
                if (getDist(point.getX(), point.getY(), clickLocation.getX(), clickLocation.getY()) <= R * R) {
                    pointIt.remove();
                    repaint();
// even if there are some other points near, I hope the user will not try to delete them all by one click
                    return;
                }
            }
        });
        return popup;
    }
}
