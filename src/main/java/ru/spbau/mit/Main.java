package ru.spbau.mit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Points");
        final Canvas canvas = new Canvas();
        final JMenuBar menubar = buildMenuBar(canvas);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setJMenuBar(menubar);
        frame.add(canvas);

        frame.setSize(1200, 600);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static JMenuBar buildMenuBar(Canvas canvas) {
        // Return JMenuBar with one JMenu called "Main"
        // This JMenu should contain "Calculate" and "Clear" JMenuItems which call same methods in Canvas
        JMenuItem itemCalc = new JMenuItem("Calculate");
        JMenuItem itemClear = new JMenuItem("Clear");

        itemClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.clear();
            }
        });

        itemCalc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.calculate();
            }
        });

        JMenu menu = new JMenu("Menu");
        menu.add(itemCalc);
        menu.add(itemClear);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        return menuBar;
    }
}
