package ru.spbau.mit;

import javax.swing.*;
import java.awt.event.ActionEvent;

public final class Main {
    private static final String MENU = "Menu";
    private static final String CALCULATE = "Build";
    private static final String CLEAR = "Clear";

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
        JMenuItem calculateItem = new JMenuItem(CALCULATE);
        calculateItem.addActionListener((ActionEvent actionEvent) -> {
            canvas.calculate();
        });

        JMenuItem clearItem = new JMenuItem(CLEAR);
        clearItem.addActionListener((ActionEvent actionEvent) -> {
            canvas.clear();
        });

        JMenu menu = new JMenu(MENU);
        menu.add(calculateItem);
        menu.add(clearItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        return menuBar;
    }
}
