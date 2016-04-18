package ru.spbau.mit;

import javax.swing.*;

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

        JMenu mainMenu = new JMenu("Main");
        JMenuItem calculateItem = new JMenuItem("Calculate");
        calculateItem.addActionListener((e) -> canvas.calculate());
        JMenuItem clearItem = new JMenuItem("Clear");
        clearItem.addActionListener((e) -> canvas.clear());
        mainMenu.add(calculateItem);
        mainMenu.add(clearItem);

        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(mainMenu);

        return jMenuBar;
    }
}
