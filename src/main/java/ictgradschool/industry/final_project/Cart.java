package ictgradschool.industry.final_project;

import ictgradschool.industry.final_project.util.FileManager;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class Cart extends JDialog implements ProductListListener {
    private JTable cartTable;
    private MultiTableAdapter cartTableAdapter;
    private TableRowSorter<TableModel> cartTableRowSorter;
    private JLabel totalPriceLabel;
    private JPanel bottomPanel;

    private ProductsList cartProductsList;
    private BigDecimal totalPrice;
    private List<ProductListListener> listeners;
    private PointOfSale pointOfSale;
    private String receiptStr;

    public Cart(ProductsList cartList, PointOfSale pos) {
        setTitle("Point Of Sale");
        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cartProductsList = new ProductsList();
        totalPrice = new BigDecimal(0);

        pointOfSale = pos;
        pointOfSale.addListener(this);

        listeners = new ArrayList<ProductListListener>();
        cartProductsList = cartList;
        FileManager.setCartProductsList(cartProductsList);
        init();
    }

    private void init() {
        // body area
        cartTable = new JTable();
        cartTableAdapter = new MultiTableAdapter(cartProductsList);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cartTable.setModel(cartTableAdapter);

        // top area
        JLabel msgLabel = new JLabel("Shopping Cart");
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 25));

        // bottom area
        bottomPanel = new JPanel();
        JButton removeOneBtn = new JButton("Remove One");
        JButton removeAllBtn = new JButton("Remove All");
        JButton backBtn = new JButton("Back");
        JButton checkoutBtn = new JButton("Checkout");
        totalPriceLabel = new JLabel();
        calculateTotalPrice();

        bottomPanel.add(removeOneBtn);
        bottomPanel.add(removeAllBtn);
        bottomPanel.add(Box.createHorizontalStrut(100));
        bottomPanel.add(totalPriceLabel);
        bottomPanel.add(Box.createHorizontalStrut(100));
        bottomPanel.add(checkoutBtn);
        bottomPanel.add(backBtn);

        // bottom function
        removeOneBtn.addActionListener(e -> reduceProducts());
        removeAllBtn.addActionListener(e -> sendRemoveInfo());
        checkoutBtn.addActionListener(e -> checkOut());
        backBtn.addActionListener(e -> dispose());

        // assemble
        add(msgLabel, BorderLayout.NORTH);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setResizable(false);
    }

    private void sendRemoveInfo() {
        int rowIndex = cartTable.getSelectedRow();
        int confirmResult;
        if (rowIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to remove", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            confirmResult = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this line?", "Delete Item Line", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        }
        if (confirmResult == JOptionPane.YES_OPTION) {
            List<Product> cartProducts = cartProductsList.getProductsList();
            Product removedProduct = cartProducts.remove(rowIndex);
            if (cartProducts.size() > 1 && rowIndex > 0 && cartProducts.size() > rowIndex) {
                if (cartProducts.get(rowIndex).getId().equals(cartProducts.get(rowIndex - 1).getId())) {
                    int quantityPre = cartProducts.get(rowIndex - 1).getQuantity();
                    int quantityNext = cartProducts.get(rowIndex).getQuantity();
                    cartProducts.get(rowIndex - 1).setQuantity(quantityPre + quantityNext);
                    cartProducts.remove(rowIndex);
                }
            }
            renewTableAndFIle(removedProduct);
        }
    }

    private void reduceProducts() {
        int rowIndex = cartTable.getSelectedRow();
        int confirmResult = 0;
        if (rowIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to remove", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Product> cartProducts = cartProductsList.getProductsList();
//            int row = cartTable.getSelectedRow();
        Product tempProduct = cartProducts.get(rowIndex);
        int quantity = tempProduct.getQuantity();
        if (quantity > 1) {
            Product removedProduct = new Product(tempProduct);
            removedProduct.setQuantity(1);
            renewTableAndFIle(removedProduct);
            tempProduct.setQuantity(quantity - 1);
            cartTable.changeSelection(rowIndex, rowIndex, false, false);
            FileManager.rewriteFile();
        } else {
            sendRemoveInfo();
        }
    }

    private void renewTableAndFIle(Product p) {
        calculateTotalPrice();
        cartTableAdapter.fireTableDataChanged();
        notifyListeners(p);
        FileManager.setProductsList(pointOfSale.getProducts());
        FileManager.setCartProductsList(cartProductsList);
        FileManager.rewriteFile();
    }

    private void checkOut() {
        if (cartProductsList.getProductsList().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No item in the cart.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Create a file to print receipt.");
        int result = fileChooser.showSaveDialog(this);
        File file = fileChooser.getSelectedFile();

        if (result == JFileChooser.APPROVE_OPTION) {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (file.exists()) {
                int confirmResult = JOptionPane.showConfirmDialog(this, "File already exists. Do you want to reset it?", "Reset", JOptionPane.YES_NO_OPTION);
                if (confirmResult == JOptionPane.YES_OPTION) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                        String tempStr = "";
                        bw.write(tempStr);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return;
                }
            }
            FileManager.setReceiptFilePath(fileChooser.getSelectedFile().getAbsolutePath());
            combineToPrintReceipt();

            cartProductsList.removeAllProducts();
            FileManager.setProductsList(pointOfSale.getProducts());
            FileManager.setCartProductsList(cartProductsList);
            FileManager.rewriteFile();
            dispose();
        }
    }

    public void addListener(ProductListListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProductListListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Product product) {
        for (ProductListListener listener : listeners) {
            listener.onceProductRemoved(product);
        }
    }

    private void calculateTotalPrice() {
        BigDecimal bigDecimal = new BigDecimal(0);
        for (Product product : cartProductsList.getProductsList()) {
            bigDecimal = bigDecimal.add(product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())));
        }
        totalPrice = bigDecimal;
//        System.out.println("totalPrice = " + totalPrice);
        totalPriceLabel.setText("Total Price: " + totalPrice);
        bottomPanel.repaint();
    }

    public void receiveNewCartProducts(ProductsList productsList) {
        this.cartProductsList = productsList;
        calculateTotalPrice();
        cartTableAdapter.setProducts(productsList);
        cartTableAdapter.fireTableDataChanged();
    }

    private void combineToPrintReceipt() {
        Map<String, Product> filterMap = new LinkedHashMap<>();
        ProductsList tempProductsList = new ProductsList();
        List<Product> tempProducts = new ArrayList<>();

        for (Product product : cartProductsList.getProductsList()) {
            if (filterMap.containsKey(product.getId())) {
                Product existingProduct = filterMap.get(product.getId());
                existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
            } else {
                filterMap.put(product.getId(), new Product(product));
            }
        }

        filterMap.forEach((key, value) -> {
            tempProducts.add(value);
        });

        tempProductsList.setProductsList(tempProducts);

        generateReceipt(tempProductsList);
    }

    private void generateReceipt(ProductsList productsList) {
//        System.out.println("productsList.toString() = " + productsList.getProductsList().toString());
        Receipt receipt = new Receipt(productsList);
        receiptStr = receipt.toString();
        if (FileManager.getReceiptFilePath() != null) {
            File file = new File(FileManager.getReceiptFilePath());
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(receiptStr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onceProductAdded(Product product) {

    }

    @Override
    public void onceProductChanged() {

    }

    @Override
    public void onceProductRemoved(Product product) {

    }
}
