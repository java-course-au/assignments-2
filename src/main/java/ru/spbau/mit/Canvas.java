package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Canvas extends JPanel implements DefaultMouseListener {
    private List<Point> points = new ArrayList<>();
    private List<Point> stack = new ArrayList<>();
    private Point curPointForRemove;

    private final JPopupMenu popupMenu = new JPopupMenu();

    Canvas() {
        addMouseListener(this);
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
                final double eps = 10;
                for (Point point: points) {
                    if (Math.abs(point.getX() - e.getX()) < eps && Math.abs(point.getY() - e.getY()) < eps) {
                        curPointForRemove = point;
                        popupMenu.show(this, e.getX(), e.getY());
                        break;
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void calculate() {

        stack.clear();

        if (points.size() < 3) {
            stack = new ArrayList<>(points);
            repaint();
            return;
        }

        points.sort(Point::compare);
        Point leftRightPoint = points.get(0);
        points = points.stream().map(p -> Point.substract(p, leftRightPoint)).collect(Collectors.toList());
        points.remove(0);
        points.sort(Point::compareAngle);
        points.add(0, new Point(0, 0));

        stack.add(points.get(0));
        stack.add(points.get(1));

        for (int i = 2; i < points.size(); i++) {
            while (crossProduct(Point.substract(points.get(i), stack.get(stack.size() - 2)),
                    Point.substract(stack.get(stack.size() - 1), stack.get(stack.size() - 2))) <  0
                    && stack.size() > 2) {
                stack.remove(stack.size() - 1);
            }
            stack.add(points.get(i));
        }

        points = points.stream().map(p -> Point.add(p, leftRightPoint)).collect(Collectors.toList());
        stack = stack.stream().map(p -> Point.add(p, leftRightPoint)).collect(Collectors.toList());

        repaint();
    }

    public void clear() {
        points.clear();
        stack.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        for (Point point: points) {
            g.fillOval(point.getX() - 5, point.getY() - 5, 10, 10);
        }

        if (stack.size() >= 1) {
            stack.add(stack.get(0));

            for (int i = 0; i < stack.size() - 1; i++) {
                g.drawLine(stack.get(i).getX(), stack.get(i).getY(),
                        stack.get(i + 1).getX(), stack.get(i + 1).getY());
            }
            stack.remove(stack.size() - 1);
        }

//        throw new UnsupportedOperationException();
    }

    private int crossProduct(Point x, Point y) {
        return x.getX() * y.getY() - x.getY() * y.getX();
    }

    private JMenuItem buildPopupMenuItem() {

        // Return JMenuItem called "Remove point"
        // Point should be removed after click

        JMenuItem jMenuItem = new JMenuItem("Remove point");
        jMenuItem.addActionListener(e -> {
            if (curPointForRemove != null) {
                points.remove(curPointForRemove);
                curPointForRemove = null;
                repaint();
            }
        });
        return jMenuItem;
    }
}
