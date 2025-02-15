package ictgradschool.industry.final_project;

import ictgradschool.industry.final_project.util.FileManager;
import ictgradschool.industry.final_project.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

public class WelcomeScreen extends JFrame {
    private static WelcomeScreen welcomeScreen;
    private JPanel mainPanel;
    private List<String> tempInventoryStrList;
    private List<String> tempCartList;

    public static void setIsVisible(boolean visible) {
        if (welcomeScreen != null) {
            welcomeScreen.setVisible(visible);
        }
    }

    public WelcomeScreen() {
        welcomeScreen = this;
        setTitle("Welcome to PDD");
        setPreferredSize(new Dimension(1280, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        mainPanel = new JPanel();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        jumpToWelcomeScreen();
        getContentPane().add(mainPanel);
    }

    private void jumpToWelcomeScreen() {
        // remove all the things in mainPanel, because there may be things from option screen
        mainPanel.removeAll();

        // initialize GUI of the first screen
        JButton selectBtn = new JButton("Select from existing filestore");
        JButton createBtn = new JButton("Create a new filestore");

        selectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectBtn.addActionListener(e -> selectFilestore());
        createBtn.addActionListener(e -> createFilestore());

        mainPanel.add(Box.createVerticalStrut(200));
        mainPanel.add(selectBtn);
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(createBtn);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void selectFilestore() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to store data.");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            FileManager.setFilePath(fileChooser.getSelectedFile().getAbsolutePath());
        } else {
            return;
        }
        if(dealWithTempStrList()){
            jumpToOptionScreen();
        }
    }


    private void createFilestore() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Create a file to store data.");
        int result = fileChooser.showSaveDialog(this);
        File file = fileChooser.getSelectedFile();

        if (result == JFileChooser.APPROVE_OPTION) {
            FileManager.setFilePath(fileChooser.getSelectedFile().getAbsolutePath());
            FileManager.setProductsList(new ProductsList());
            FileManager.setCartProductsList(new ProductsList());


            if (!file.exists()) {
                FileManager.rewriteFile();
            } else if (file.exists()) {
                int confirmResult = JOptionPane.showConfirmDialog(this, "File already exists. Do you want to reset it?", "Reset", JOptionPane.YES_NO_OPTION);
                if (confirmResult == JOptionPane.YES_OPTION) {
                    FileManager.rewriteFile();
                } else {
                    return;
                }
            }
        } else {
            return;
        }
        jumpToOptionScreen();
    }

    private void jumpToOptionScreen() {
        // same as the ones in jumpToWelcomeScreen method
        mainPanel.removeAll();

        JButton returnBtn = new JButton("Close filestore");
        JButton inventoryBtn = new JButton("Open Inventory Manager");
        JButton posBtn = new JButton("Open Point of Sale");

        returnBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        inventoryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        posBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        returnBtn.addActionListener(e -> jumpToWelcomeScreen());
        inventoryBtn.addActionListener(e -> jumpToInventoryManager());
        posBtn.addActionListener(e -> jumpToPointOfSale());

        mainPanel.add(Box.createVerticalStrut(150));
        mainPanel.add(returnBtn);
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(inventoryBtn);
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(posBtn);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void jumpToInventoryManager() {
        if (dealWithTempStrList()) {
            setVisible(false);

            SwingUtilities.invokeLater(() -> {
                InventoryManager inventoryManager = new InventoryManager(tempInventoryStrList);
                inventoryManager.setVisible(true);
            });
        }
    }

    private void jumpToPointOfSale() {
        if (dealWithTempStrList()) {
            setVisible(false);

            SwingUtilities.invokeLater(() -> {
                PointOfSale pointOfSale = new PointOfSale(tempInventoryStrList);
                pointOfSale.setVisible(true);
            });
        }
    }

    // same logic in two places, so Encapsulated in a method
    private boolean dealWithTempStrList() {
        ProductsList productsList = FileManager.getCurrentInventoryContent();
        if(productsList == null){
            JOptionPane.showMessageDialog(null, "Invalid File Format. Please select a valid File");
            return false;
        }else {
            FileManager.setProductsList(productsList);
            FileManager.setCartProductsList(FileManager.getCurrentCartContent());
            return true;
        }
    }
}
