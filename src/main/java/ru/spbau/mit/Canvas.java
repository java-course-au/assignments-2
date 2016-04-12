package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Canvas extends JPanel implements DefaultMouseListener {
    private static final String REMOVE_POINT = "Remove point";
    private static final int POINT_RADIUS = 5;
    private static final int REMOVE_RADIUS = 10;
    private static final int LINE_WIDTH = 3;

    private final JPopupMenu popupMenu = new JPopupMenu();
    private final List<Point> pointsList = new ArrayList<>();
    private final List<Point> convexHullList = new ArrayList<>();
    private Point clickLocation;

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                pointsList.add(new Point(e.getX(), e.getY()));
                break;
            case MouseEvent.BUTTON3:
                clickLocation = new Point(e.getX(), e.getY());
                popupMenu.show(this, e.getX(), e.getY());
            default:
                throw new UnsupportedOperationException();
        }
        repaint();
    }

    public void calculate() {
        if (pointsList.size() == 0) {
            return;
        }
        Point p = pointsList.get(0);
        for (Point point : pointsList) {
            if (point.getX() < p.getX() || (point.getX() == p.getX() && point.getY() < p.getY())) {
                p = point;
            }
        }
        final Point start = p;
        pointsList.remove(start);
        Collections.sort(pointsList, (Point p1, Point p2) -> {
            double angle1 = p1.subtract(start).getAngle();
            double angle2 = p2.subtract(start).getAngle();
            if (angle1 != angle2) {
                return Double.compare(angle1, angle2);
            }
            return Double.compare(p1.subtract(start).getLength(), p2.subtract(start).getLength());
        });
        pointsList.add(0, p);
        convexHullList.clear();
        for (Point point : pointsList) {
            while (convexHullList.size() > 1) {
                Point p1 = convexHullList.get(convexHullList.size() - 2);
                Point p2 = convexHullList.get(convexHullList.size() - 1);
                if ((p2.getX() - p1.getX()) * (point.getY() - p1.getY())
                        - (point.getX() - p1.getX()) * (p2.getY() - p1.getY()) > 0) {
                    break;
                }
                convexHullList.remove(convexHullList.size() - 1);
            }
            convexHullList.add(point);
        }

        repaint();
    }

    public void clear() {
        pointsList.clear();
        convexHullList.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);

        for (Point point : pointsList) {
            System.out.println(point.getX());
            System.out.println(point.getY());
            g.fillOval(point.getX() - POINT_RADIUS, point.getY() - POINT_RADIUS,
                    2 * POINT_RADIUS, 2 * POINT_RADIUS);
        }

        if (convexHullList.size() == 0) {
            return;
        }
        g.setColor(Color.RED);

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(LINE_WIDTH));

        convexHullList.add(convexHullList.get(0));
        for (int i = 0; i < convexHullList.size() - 1; i++) {
            g2.drawLine(convexHullList.get(i).getX(), convexHullList.get(i).getY(),
                    convexHullList.get(i + 1).getX(), convexHullList.get(i + 1).getY());
        }
        convexHullList.remove(convexHullList.size() - 1);
    }

    private JMenuItem buildPopupMenuItem() {
        JMenuItem removePointItem = new JMenuItem(REMOVE_POINT);
        removePointItem.addActionListener((ActionEvent actionEvent) -> {
            if (pointsList.size() == 0) {
                return;
            }
            double minimumDistance = (pointsList.get(0).subtract(clickLocation)).getLength();
            Point p = pointsList.get(0);
            for (Point point : pointsList) {
                double length = point.subtract(clickLocation).getLength();
                if (length < minimumDistance) {
                    minimumDistance = length;
                    p = point;
                }
            }
            if (minimumDistance < REMOVE_RADIUS) {
                pointsList.remove(p);
            }
            repaint();
        });

        return removePointItem;
    }
}
