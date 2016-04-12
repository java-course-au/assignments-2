package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;

/**
 * Created by ldvsoft on 12.04.16.
 */
public class MyPopupMenu extends JPopupMenu {
    private Point lastPoint;

    @Override
    public void show(Component invoker, int x, int y) {
        lastPoint = new Point(x, y);
        super.show(invoker, x, y);
    }

    public Point getLastPoint() {
        return lastPoint;
    }
}
