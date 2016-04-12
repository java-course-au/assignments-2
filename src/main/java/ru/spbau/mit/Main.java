package ru.spbau.mit;

import javax.swing.*;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Points");
        final Canvas canvas = new Canvas();
        final JMenuBar menubar = buildMenuBar(canvas);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(() -> {
            frame.setJMenuBar(menubar);
            frame.add(canvas);

            frame.setSize(1200, 600);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }

    private static JMenuBar buildMenuBar(Canvas canvas) {
        // Return JMenuBar with one JMenu called "Main"
        // This JMenu should contain "Calculate" and "Clear" JMenuItems which call same methods in Canvas
        JMenuBar menuBar = new JMenuBar();
        JMenu mainMenu = new JMenu("Main");
        menuBar.add(mainMenu);

        JMenuItem calculate = new JMenuItem("Calculate");
        mainMenu.add(calculate);
        calculate.addActionListener(e -> {
            canvas.calculate();
        });
        JMenuItem clear = new JMenuItem("Clear");
        mainMenu.add(clear);
        clear.addActionListener(e -> {
            canvas.clear();
        });

        return menuBar;
    }
}
