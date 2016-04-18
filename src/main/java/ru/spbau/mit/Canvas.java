package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

class Canvas extends JPanel implements DefaultMouseListener {

    private static final String REMOVE_POINT_JMENU_ITEM = "Remove point";
    private static final int POINT_DRAW_RADIUS = 5;
    private static final int POINT_REMOVE_RADIUS = 15;

    private final JPopupMenu popupMenu = new JPopupMenu();
    private final List<Point> pointList = new ArrayList<>();
    private final List<Point> convexHullPoints = new ArrayList<>();

    private Point popupMenuAssignee = null;

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
    }

    private void searchPointOnCanvasClicked(Point position) {
        pointList.forEach(point -> {
            if (point.sub(position).getDist() < POINT_REMOVE_RADIUS) {
                popupMenuAssignee = point;
                popupMenu.show(this, point.getX(), point.getY());
            }
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point location = new Point(e.getX(), e.getY());
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                pointList.add(location);
                break;
            case MouseEvent.BUTTON3:
                searchPointOnCanvasClicked(location);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        repaint();
    }

    public void calculate() {
        convexHullPoints.clear();
        if (!pointList.isEmpty()) {
            List<Point> tmpList = new ArrayList<>(pointList);
            Point start = tmpList.stream().min(Point.LEFT_DOWN_COMPARATOR).get();
            tmpList.remove(start);
            convexHullPoints.add(start);

            tmpList.stream().sorted(Point.getAngleComparator(start)).forEach(point -> {
                while (convexHullPoints.size() > 2) {
                    Point p1 = convexHullPoints.get(convexHullPoints.size() - 2);
                    Point p2 = convexHullPoints.get(convexHullPoints.size() - 1);
                    if (point.cross(p1, p2) > 0) {
                        break;
                    }
                    convexHullPoints.remove(convexHullPoints.size() - 1);
                }
                convexHullPoints.add(point);
            });
        }
        repaint();
    }

    public void clear() {
        pointList.clear();
        convexHullPoints.clear();
        repaint();
    }

    private void drawPoint(Point point, Graphics g) {
        g.fillOval(point.getX(), point.getY(), 2 * POINT_DRAW_RADIUS, 2 * POINT_DRAW_RADIUS);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        pointList.forEach(point -> drawPoint(point, g));
        for (int i = 0; i < convexHullPoints.size(); i++) {
            int next = (i + 1) % convexHullPoints.size();
            g.drawLine(convexHullPoints.get(i).getX(), convexHullPoints.get(i).getY(),
                    convexHullPoints.get(next).getX(), convexHullPoints.get(next).getY());
        }
    }

    private void popupMenuItemActionListener(ActionEvent actionEvent) {
        pointList.remove(pointList.indexOf(popupMenuAssignee));
        repaint();
    }

    private JMenuItem buildPopupMenuItem() {
        JMenuItem jMenuItem = new JMenuItem(REMOVE_POINT_JMENU_ITEM);
        jMenuItem.addActionListener(this::popupMenuItemActionListener);
        return jMenuItem;
    }
}
