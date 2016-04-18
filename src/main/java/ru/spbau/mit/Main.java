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
        JMenuBar menuBar = new JMenuBar();

        JMenuItem calculate = new JMenuItem("Calculate");
        calculate.addActionListener((a) -> canvas.calculate());
        menuBar.add(calculate);

        JMenuItem clear = new JMenuItem("Clear");
        clear.addActionListener((a) -> canvas.clear());
        menuBar.add(clear);
        return menuBar;
    }
}
