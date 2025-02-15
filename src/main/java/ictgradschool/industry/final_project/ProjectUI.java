package ictgradschool.industry.final_project;

import javax.swing.*;

public class ProjectUI {
    public static void main(String[] args) {
        // TODO: Your code here
        SwingUtilities.invokeLater(() -> {
            WelcomeScreen w = new WelcomeScreen();
            w.setVisible(true);
        });
    }
}
