package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

class Canvas extends JPanel implements DefaultMouseListener {
    private List<Point> points = new ArrayList<>();
    private ArrayList<Point> stack = new ArrayList<>();
    private Point curPointForRemove;

    private final JPopupMenu popupMenu = new JPopupMenu();
    private final double EPS = 1e-9;

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
/*                for (Point point: points) {
                    if (Math.abs(point.getX() - e.getX()) == 0)
                } */
                throw new UnsupportedOperationException();
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
        stack.add(points.get(0));
        stack.add(points.get(1));
        for (int i = 2; i < points.size(); i++) {
            while (stack.size() >= 2 && crossProduct(
                    Point.substract(stack.get(stack.size() - 1), stack.get(stack.size() - 2)),
                    Point.substract(points.get(i), stack.get(stack.size() - 2))) < 0) {
                stack.remove(stack.size() - 1);
            }
            stack.add(points.get(i));
        }
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

        if (points.size() <= 2) {
            stack = new ArrayList<>(points);
            repaint();
            return;
        }

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        for (Point point: points) {
            g.fillOval(point.getX() - 5, point.getY() - 5, 10, 10);
        }

        points.sort(Point::compare);
        Point leftRightPoint = points.get(0);
        points = points.stream().map(p -> Point.substract(p, leftRightPoint)).collect(Collectors.toList());   //??



        if (stack.size() >= 1) {
            stack.add(stack.get(0));

            for (int i = 0; i < stack.size() - 1; i++) {
                g.drawLine(stack.get(i).getX(), stack.get(i).getY(), stack.get(i + 1).getX(), stack.get(i + 1).getY());
            }
        }

//        throw new UnsupportedOperationException();
    }

    private int crossProduct(Point x, Point y) {
        return x.getX()*y.getY() - x.getY()*y.getX();
    }

    private JMenuItem buildPopupMenuItem() {

        // Return JMenuItem called "Remove point"
        // Point should be removed after click

//        throw new UnsupportedOperationException();
        return new JMenuItem();
    }
}
