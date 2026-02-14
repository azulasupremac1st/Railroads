package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/*
        GLEDAJ LINE 10
 */

import static java.lang.Math.floor;

public class Main {
    //public static final int TILE_SIZE = 64;
    //public static final int GRID_PADDING = 40;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Railroads");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel root = new JPanel(cardLayout);

        WelcomePanel welcomePanel = new WelcomePanel(root, cardLayout);
        root.add(welcomePanel, "WELCOME");

        /*

        ~ the constraints are your sort of identifiers that are necessary in a CardLayout
        ~ it basically works like a hash map

        */

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
