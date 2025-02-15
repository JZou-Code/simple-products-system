package ictgradschool.industry.final_project;

import ictgradschool.industry.final_project.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ProductCreator extends JDialog {
    private ProductsList products;

    private List<ProductListListener> listeners;

    public ProductCreator(ProductsList productslist) {
        setTitle("Add New Product");
        setPreferredSize(new Dimension(600, 400));
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        this.products = productslist;
        listeners = new ArrayList<>();

        initUI();
    }

    private void initUI() {
        JLabel msgLabel = new JLabel("CREATE NEW PRODUCT");
        msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel idLabel = new JLabel("ID (10-digit or blank):");
        JLabel nameLabel = new JLabel("Name of product:");
        JLabel descriptionLabel = new JLabel("Description:");
        JLabel priceLabel = new JLabel("Price (two decimal):");
        JLabel quantityLabel = new JLabel("Quantity (integer):");

        JTextField idField = new JTextField(35);
        JTextField nameField = new JTextField(35);
        JTextField descriptionField = new JTextField(35);
        JTextField priceField = new JTextField(35);
        JTextField quantityField = new JTextField(35);

        JPanel bodyPanel1 = new JPanel();
        JPanel bodyPanel2 = new JPanel();
        JPanel bodyPanel3 = new JPanel();
        JPanel bodyPanel4 = new JPanel();
        JPanel bodyPanel5 = new JPanel();


        Box box = Box.createVerticalBox();

        bodyPanel1.add(idLabel);
        bodyPanel1.add(idField);
        bodyPanel2.add(nameLabel);
        bodyPanel2.add(nameField);
        bodyPanel3.add(descriptionLabel);
        bodyPanel3.add(descriptionField);
        bodyPanel4.add(priceLabel);
        bodyPanel4.add(priceField);
        bodyPanel5.add(quantityLabel);
        bodyPanel5.add(quantityField);

        box.add(bodyPanel1);
        box.add(bodyPanel2);
        box.add(bodyPanel3);
        box.add(bodyPanel4);
        box.add(bodyPanel5);
        box.add(Box.createVerticalStrut(100));

        bodyPanel1.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bodyPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bodyPanel3.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bodyPanel4.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bodyPanel5.setLayout(new FlowLayout(FlowLayout.RIGHT));

        // button part
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalStrut(80));
        buttonPanel.add(cancelButton);

        addButton.addActionListener(e -> {
            String id = idField.getText();
            String name = nameField.getText();
            String description = descriptionField.getText();
            String price = priceField.getText();
            String quantity = quantityField.getText();
            passToInventoryManager(id, name, description, price, quantity);
        });

        cancelButton.addActionListener(e -> dispose());

        // assemble
        add(msgLabel, BorderLayout.NORTH);
        add(box, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void addListener(ProductListListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProductListListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Product product) {
        for (ProductListListener listener : listeners) {
            listener.onceProductAdded(product);
        }
    }

    private void passToInventoryManager(String id, String name, String description, String priceStr, String quantityStr) {
        if("".equals(id)){
            id = idGenerator();
        }else if (!Validator.validateIDFormat(id, products.getProductsList())) {
            JOptionPane.showMessageDialog(this, "The ID must be a unique 10-character identifier, consisting only of numbers and uppercase letters.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Validator.validatePriceFormat(priceStr)) {
            JOptionPane.showMessageDialog(this, "Price must be a non-negative number with up to two decimal places.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Validator.validateQuantityFormat(quantityStr)) {
            JOptionPane.showMessageDialog(this, "Quantity must be a non-negative Integer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal tempPrice = new BigDecimal(priceStr.trim());
        BigDecimal price = tempPrice.setScale(2, RoundingMode.HALF_UP);
        int quantity = Integer.parseInt(quantityStr.trim());
        Product product = new Product(id.toUpperCase(), name, description, price, quantity);

        notifyListeners(product);
        this.dispose();
    }

    private String idGenerator() {
        String newID;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        do{
            newID = "";
        for (int i = 0; i < 10; i++) {
            int index = (int) (Math.random() * chars.length());
            newID += chars.charAt(index);
        }}while (products.getProductsList().contains(newID));
        return newID;
    }
}
