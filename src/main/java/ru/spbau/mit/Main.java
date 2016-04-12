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

        JMenu menu = new JMenu("Main");
        JMenuItem itemCalculate = new JMenuItem("Calculate");
        itemCalculate.addActionListener((event) -> {
            canvas.calculate();
        });
        menu.add(itemCalculate);

        JMenuItem itemClear = new JMenuItem("Clear");
        itemClear.addActionListener((event) -> {
            canvas.clear();
        });
        menu.add(itemClear);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        return menuBar;
    }
}
