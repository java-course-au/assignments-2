package ru.spbau.mit;

import javax.swing.*;

public final class Main {
    private static final String MAIN_MENU = "Main";
    private static final String CALCULATE_MENU_ITEM = "Calculate";
    private static final String CLEAR_MENU_ITEM = "Clear";

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

    private static JMenu buildJMenu(Canvas canvas) {
        JMenu jMenu = new JMenu(MAIN_MENU);
        JMenuItem calculateMenuItem = new JMenuItem(CALCULATE_MENU_ITEM);
        JMenuItem clearMenuItem = new JMenuItem(CLEAR_MENU_ITEM);
        calculateMenuItem.addActionListener(e -> canvas.calculate());
        clearMenuItem.addActionListener(e -> canvas.clear());
        jMenu.add(calculateMenuItem);
        jMenu.add(clearMenuItem);
        return jMenu;
    }

    private static JMenuBar buildMenuBar(Canvas canvas) {
        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(buildJMenu(canvas));
        return jMenuBar;
    }
}
