package ru.spbau.mit;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class Canvas extends JPanel implements DefaultMouseListener {
    private static final int POINT_RADIUS = 5;
    private static final int POINT_DELETE_RADIUS = 10;

    private final Set<Point> points = new HashSet<>();
    private Shape convexHull = new GeneralPath();

    Canvas() {
        addMouseListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point currentPoint = new Point(e.getX(), e.getY());
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                points.add(currentPoint);
                repaint();
                break;
            case MouseEvent.BUTTON3:
                JPopupMenu menu = buildPopupMenu(currentPoint);
                if (menu != null) {
                    menu.show(this, e.getX(), e.getY());
                }
                break;
            default:
                break;
        }
    }

    private JPopupMenu buildPopupMenu(Point currentPoint) {
        for (Point p : points) {
            if (p.distanceTo(currentPoint) <= POINT_DELETE_RADIUS) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem remove = new JMenuItem("Remove point");
                remove.addActionListener((a) -> {
                    points.remove(p);
                    repaint();
                });
                menu.add(remove);
                return menu;
            }
        }
        return null;
    }

    public void calculate() {
        Coordinate[] coordinates =
            points.stream()
                .map(p -> new Coordinate(p.getX(), p.getY()))
                .collect(Collectors.toList())
                .toArray(new Coordinate[points.size()]);
        Geometry convexHull = new ConvexHull(coordinates, new GeometryFactory()).getConvexHull();
        this.convexHull = new ShapeWriter().toShape(convexHull);
        repaint();
    }

    public void clear() {
        points.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());

        for (Point p : points) {
            int x1 = p.getX() - POINT_RADIUS;
            int y1 = p.getY() - POINT_RADIUS;
            int size = POINT_RADIUS * 2;

            g.setColor(new Color(192, 192, 255));
            g.fillOval(x1, y1, size, size);
            g.setColor(Color.BLACK);
            g.drawOval(x1, y1, size, size);
        }
        g.setColor(Color.RED);
        ((Graphics2D) g).draw(convexHull);
    }
}
