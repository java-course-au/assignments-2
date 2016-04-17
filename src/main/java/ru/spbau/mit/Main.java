package ru.spbau.mit;

import javax.swing.*;
import java.util.logging.Logger;

public final class Main {

    private static final Logger LOG = Logger.getLogger("Main");

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

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem calculate = new JMenuItem("Calculate");
        calculate.addActionListener(e -> canvas.calculate());
        JMenuItem clear = new JMenuItem("Clear");
        clear.addActionListener(e -> {
            canvas.clear();
            LOG.info("Cleared");
        });
        menu.add(calculate);
        menu.add(clear);
        menuBar.add(menu);

        return menuBar;
    }
}
