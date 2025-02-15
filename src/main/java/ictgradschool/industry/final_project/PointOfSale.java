package ictgradschool.industry.final_project;

import ictgradschool.industry.final_project.util.FileManager;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class PointOfSale extends JFrame implements ProductListListener {
    private JTable pointOfSaleTable;
    private MultiTableAdapter pOSTableAdapter;
    private TableRowSorter<TableModel> pOSTableRowSorter;
    private Cart cart;

    private List<String> filteredProductsStrings;
    private List<String> filteredCartStrings;
    private ProductsList products;
    private ProductsList cartProducts;
    private List<ProductListListener> listeners;


    public PointOfSale(List<String> tempStrList) {
        setTitle("Point Of Sale");
        setPreferredSize(new Dimension(1280, 720));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                returnToWelcomeScreen();
            }
        });

        filteredProductsStrings = tempStrList;

        products = FileManager.getProductsList();
        products.addListener(this);
        cartProducts = FileManager.getCartProductsList();

        listeners = new ArrayList<>();
        initUI();
    }

    public ProductsList getProducts() {
        return products;
    }

    public void setProducts(ProductsList products) {
        this.products = products;
    }

    private void initUI() {
        // body area
        pointOfSaleTable = new JTable();
        pointOfSaleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pOSTableAdapter = new MultiTableAdapter(products);
        pointOfSaleTable.setModel(pOSTableAdapter);

        pOSTableRowSorter = new TableRowSorter<>(pOSTableAdapter);
        pointOfSaleTable.setRowSorter(pOSTableRowSorter);

        pOSTableRowSorter.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, 0, 4));

        // top area
        JLabel msgLabel = new JLabel("TIME TO DO SOME SHOPPING!");
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 25));

        // bottom area
        JPanel bottomPanel = new JPanel();
        JButton addBtn = new JButton("Add to Cart");
        JButton cartBtn = new JButton("View Cart");
        JButton backBtn = new JButton("Back");

        bottomPanel.add(addBtn);
        bottomPanel.add(cartBtn);
        bottomPanel.add(backBtn);

        // bottom function
        addBtn.addActionListener(e -> addToCart());
        cartBtn.addActionListener(e -> openCartView());
        backBtn.addActionListener(e -> returnToWelcomeScreen());

        // assemble
        add(msgLabel, BorderLayout.NORTH);
        add(new JScrollPane(pointOfSaleTable), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addToCart() {
        int row = pointOfSaleTable.getSelectedRow();
        int rowIndex;

        if (row == -1) {
            JOptionPane.showMessageDialog(pointOfSaleTable, "Please select one item");
            return;
        } else {
            rowIndex = pointOfSaleTable.convertRowIndexToModel(row);
            int currentQ = products.getProductsList().get(rowIndex).getQuantity();
            products.getProductsList().get(rowIndex).setQuantity(currentQ - 1);

            if (currentQ > 1) {
                pOSTableAdapter.fireTableCellUpdated(rowIndex, 4);
            } else {
            pOSTableAdapter.fireTableDataChanged();
            }
        }

        Product product = new Product(products.getProductsList().get(rowIndex));
        product.setQuantity(1);
        cartProducts.addProduct(product);
        combineCartProducts();
        sendInfoToCart();
        FileManager.setProductsList(products);
        FileManager.setCartProductsList(cartProducts);
        FileManager.rewriteFile();
    }

    private void sendInfoToCart() {
        if(cart != null) {
            cart.receiveNewCartProducts(cartProducts);
        }
    }

    private void combineCartProducts() {
        List<Product> tempProductsList = cartProducts.getProductsList();
        int size = tempProductsList.size();
        if (tempProductsList.size() > 1) {
            if (tempProductsList.get(size - 1).getId().equals(tempProductsList.get(size - 2).getId())) {
                Product tempProduct = tempProductsList.get(size - 2);
                tempProduct.setQuantity(tempProductsList.get(size - 2).getQuantity() + 1);
                cartProducts.removeProduct(size - 1);
                cartProducts.removeProduct(size - 2);
                cartProducts.addProduct(tempProduct);
            }
        }
    }

    private void openCartView() {
//        filteredCartStrings = FileManager.getCurrentCartContent();

        SwingUtilities.invokeLater(() -> {
            cart = new Cart(cartProducts, this);
            cart.addListener(this);
            cart.setVisible(true);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    cart.dispose();
                }
            });
        });
    }


    public void returnToWelcomeScreen() {
        dispose();
        WelcomeScreen.setIsVisible(true);
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

    @Override
    public void onceProductAdded(Product product) {

    }

    @Override
    public void onceProductChanged() {

    }

    @Override
    public void onceProductRemoved(Product product) {
        int quantity = product.getQuantity();
        String id = product.getId();
        for (Product p : products.getProductsList()) {
            if (p.getId().equals(id)) {
                int currentQ = p.getQuantity();
                p.setQuantity(currentQ + quantity);
            }
        }
        pOSTableAdapter.fireTableDataChanged();
    }
}
