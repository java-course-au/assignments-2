package ru.spbau.mit;

import javax.swing.*;

public final class Main {
    private Main() {

    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Points");
        final Canvas canvas = new Canvas();
        final JMenuBar menuBar = buildMenuBar(canvas);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setJMenuBar(menuBar);
        frame.add(canvas);

        frame.setSize(1200, 600);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static JMenuBar buildMenuBar(Canvas canvas) {
        // Return JMenuBar with one JMenu called "Main"
        // This JMenu should contain "Calculate" and "Clear" JMenuItems which call same methods in Canvas

        JMenuItem calculateItem = new JMenuItem("Calculate");
        calculateItem.addActionListener((e) -> canvas.calculate());

        JMenuItem clearItem = new JMenuItem("Clear");
        clearItem.addActionListener((e) -> canvas.clear());

        JMenu jMenu = new JMenu("Menu");
        jMenu.add(calculateItem);
        jMenu.add(clearItem);

        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(jMenu);
        return jMenuBar;
    }
}
