package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

class Canvas extends JPanel implements DefaultMouseListener {

    private class PopupMenu extends JPopupMenu {
        private Point clickPoint;

        @Override
        public void show(Component invoker, int x, int y) {
            for (Point point : points) {
                if (point.distance2(x, y) <= SIZE * SIZE) {
                    clickPoint = new Point(x, y);
                    super.show(invoker, x, y);
                    return;
                }
            }
        }

        public Point getClickPoint() {
            return clickPoint;
        }
    }

    private static final int SIZE = 10;
    private final PopupMenu popupMenu = new PopupMenu();
    private ArrayList<Point> points = new ArrayList<Point>();
    private ArrayList<Line> lines = new ArrayList<Line>();

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
        setComponentPopupMenu(popupMenu);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            points.add(new Point(e.getX(), e.getY()));
            repaint();
        }
    }

    public void calculate() {
        lines.clear();
        if (points.isEmpty()) {
            return;
        }
        final Point pointOrigin = new Point(Collections.min(points));
        for (Point point : points) {
            point.sub(pointOrigin);
        }
        Collections.sort(points, (a, b) -> {
            int cp = b.crossProduct(a);
            if (cp != 0) {
                return cp;
            }
            return a.length2() - b.length2();
        });
        for (Point point : points) {
            point.add(pointOrigin);
        }

        ArrayList<Point> stack = new ArrayList<>();
        for (Point point : points) {
            while (stack.size() >= 2
                    && (new Point(stack.get(stack.size() - 2), stack.get(stack.size() - 1))).
                    crossProduct(new Point(stack.get(stack.size() - 1), point)) <= 0) {
                stack.remove(stack.size() - 1);
            }
            stack.add(point);
        }

        for (int i = 0; i < stack.size(); i++) {
            lines.add(new Line(stack.get(i), stack.get((i + 1) % stack.size())));
        }

        repaint();
    }

    public void clear() {
        points.clear();
        lines.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        for (Point point : points) {
            g.fillOval(point.getX() - SIZE / 2, point.getY() - SIZE / 2, SIZE, SIZE);
        }
        for (Line line : lines) {
            g.drawLine(line.getA().getX(), line.getA().getY(), line.getB().getX(), line.getB().getY());
        }
    }

    private void removePoint(ActionEvent e) {
        Iterator<Point> iterator = points.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            if (point.distance2(popupMenu.getClickPoint()) <= SIZE * SIZE) {
                iterator.remove();
            }
        }
        repaint();
    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click

        JMenuItem jMenuItem = new JMenuItem("Remove point");
        jMenuItem.addActionListener(this::removePoint);
        return jMenuItem;
    }
}
