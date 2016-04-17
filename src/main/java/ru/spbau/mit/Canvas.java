package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Canvas extends JPanel implements DefaultMouseListener {
    private static final int EPSILON = 5;
    private final JPopupMenu popupMenu = new JPopupMenu();
    private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Point> stack = new ArrayList<>();

    private Point mousePosition;

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                points.add(new Point(e.getX(), e.getY()));
                stack.clear();
                repaint();
                break;
            case MouseEvent.BUTTON3:
                mousePosition = new Point(e.getX(), e.getY());
                popupMenu.show(this, e.getX(), e.getY());
                break;
            default:
        }
    }

    private int crossProduct(Point a, Point b) {
        return a.getX() * b.getY() - a.getY() * b.getX();
    }

    public void calculate() {
        if (points.size() <= 1) {
            return;
        }
        ArrayList<Point> copy = new ArrayList<>();
        for (Point p : points) {
            copy.add(p);
        }

        Point min = copy.get(0);
        for (Point aCopy : copy) {
            if (aCopy.compareTo(min) < 0) {
                min = aCopy;
            }
        }

        for (int i = 0; i < copy.size(); ++i) {
            copy.set(i, (copy.get(i).subtract(min)));
        }

        Collections.sort(copy, new Comparator<Point>() {
            public int compare(Point a, Point b) {
                if (crossProduct(a, b) < 0) {
                    return 1;
                } else if (crossProduct(a, b) > 0) {
                    return -1;
                } else {
                    if (a.getDistanceSquare() < b.getDistanceSquare()) {
                        return -1;
                    } else if (a.getDistanceSquare() > b.getDistanceSquare()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        });

        stack.clear();

        stack.add(copy.get(0));
        stack.add(copy.get(1));

        copy.add(copy.get(0));
        for (int i = 2; i < (int) copy.size(); ++i) {
            Point p = copy.get(i);
            while (stack.size() >= 2 && !(crossProduct(p.subtract(stack.get(stack.size() - 1)),
                    stack.get(stack.size() - 2).subtract(stack.get(stack.size() - 1))) > 0)) {
                stack.remove(stack.size() - 1);
            }
            stack.add(p);
        }

        for (int i = 0; i < stack.size(); ++i) {
            stack.set(i, stack.get(i).add(min));
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

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);

        for (Point point : points) {
            g.fillOval(point.getX() - 5, point.getY() - 5, 10, 10);
        }

        for (int i = 1; i < stack.size(); ++i) {
            Point p1 = stack.get(i - 1);
            Point p2 = stack.get(i);
            g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }
    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click

        JMenuItem item = new JMenuItem("Remove point");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < points.size(); ++i) {
                    if (points.get(i).subtract(mousePosition).getDistanceSquare() < EPSILON * EPSILON) {
                        points.remove(i);
                        stack.clear();
                        repaint();
                        return;
                    }
                }
            }
        });
        return item;
    }
}
