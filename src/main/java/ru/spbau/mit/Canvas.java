package ru.spbau.mit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

class Canvas extends JPanel implements DefaultMouseListener {

    private final JPopupMenu popupMenu = new JPopupMenu();

    Canvas() {
        addMouseListener(this);
        popupMenu.add(buildPopupMenuItem());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                throw new UnsupportedOperationException();
            case MouseEvent.BUTTON3:
                throw new UnsupportedOperationException();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void calculate() {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);

        throw new UnsupportedOperationException();
    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click

        throw new UnsupportedOperationException();
    }
}
