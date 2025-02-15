package ictgradschool.industry.final_project;

import ictgradschool.industry.final_project.util.FileManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

public class InventoryManager extends JFrame implements ProductListListener {
    private JTable inventoryTable;
    private JTextField searcher;
    private MultiTableAdapter inventoryTableAdapter;
    private TableRowSorter<TableModel> inventoryTableRowSorter;


    private List<String> filteredProductsStrings;
    private ProductsList products;
    private int command;

    public InventoryManager(List<String> tempStrList) {
        setTitle("Inventory Information");
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
        initUI();
    }

    public List<Product> getProducts() {
        return products.getProductsList();
    }

    public void setProducts(List<Product> products) {
        this.products.setProductsList(products);
    }

    private void initUI() {
        // center area, table
        inventoryTable = new JTable();
        inventoryTableAdapter = new MultiTableAdapter(products);
        inventoryTableAdapter.setEditable(true);
        inventoryTable.setModel(inventoryTableAdapter);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        inventoryTableRowSorter = new TableRowSorter<>(inventoryTableAdapter);
        inventoryTable.setRowSorter(inventoryTableRowSorter);

        products.setNotifyEnabled(true);

        // top area, search text field, search button and clear button
        JPanel topPanel = new JPanel();
        searcher = new JTextField(50);
        JButton clearBtn = new JButton("Clear");
        JButton filterBtn = getFilterBtn();

        // add top functionalities
        searcher.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showFilteredInfo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showFilteredInfo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                showFilteredInfo();
            }
        });
        clearBtn.addActionListener(e -> searcher.setText(""));

        // assemble top area
        topPanel.add(searcher);
        topPanel.add(clearBtn);
        topPanel.add(filterBtn);

        // bottom area, add button, remove button and back button
        JPanel bottomPanel = new JPanel();
        JButton addItemBtn = new JButton("Add Item");
        JButton removeItemBtn = new JButton("Remove Item");
        JButton backBtn = new JButton("Back");

        // add bottom function
        addItemBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                ProductCreator productCreator = new ProductCreator(products);
                productCreator.addListener(this);
                productCreator.setModal(true);
                productCreator.setVisible(true);
            });
        });

        removeItemBtn.addActionListener(e -> removeProduct());

        backBtn.addActionListener(e -> returnToWelcomeScreen());

        bottomPanel.add(addItemBtn);
        bottomPanel.add(Box.createHorizontalStrut(30));
        bottomPanel.add(removeItemBtn);
        bottomPanel.add(Box.createHorizontalStrut(30));
        bottomPanel.add(backBtn);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton getFilterBtn() {
        JButton filterBtn = new JButton("Filter");

        // filter popup menu
        JPopupMenu filterPopupMenu = new JPopupMenu();
        JMenuItem allItem = new JMenuItem("All");
        JMenuItem inStoreItem = new JMenuItem("Available");
        JMenuItem outStoreItem = new JMenuItem("Out Of Stock");

        filterPopupMenu.add(allItem);
        filterPopupMenu.add(inStoreItem);
        filterPopupMenu.add(outStoreItem);

        allItem.addActionListener(e -> {
            command = 0;
            showFilteredInfo();
        });
        inStoreItem.addActionListener(e -> {
            command = 1;
            showFilteredInfo();
        });
        outStoreItem.addActionListener(e -> {
            command = 2;
            showFilteredInfo();
        });

        filterBtn.addActionListener(e ->
                filterPopupMenu.show(filterBtn, 0, filterBtn.getHeight()));

        return filterBtn;
    }

    private void removeProduct() {
        int rowIndex = inventoryTable.getSelectedRow();
        int confirmResult = 0;
        if (rowIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to remove", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            confirmResult = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item (product id: " + products.getProductsList().get(inventoryTable.getSelectedRow()).getId() + ")?", "Delete Item", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        }
        if (confirmResult == JOptionPane.YES_OPTION) {
            products.removeProduct(rowIndex);
            inventoryTableAdapter.fireTableDataChanged();
            FileManager.setProductsList(products);
            FileManager.rewriteFile();
        }
    }

    public void returnToWelcomeScreen() {
        dispose();
        WelcomeScreen.setIsVisible(true);
    }

    private void showFilteredInfo() {
        // store info of filter rule from filter button
        RowFilter<TableModel, Object> buttonFilter = null;
        switch (command) {
            case 1:
                buttonFilter = RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, 0, 4);
                break;
            case 2:
                buttonFilter = RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, 0, 4);
        }

        // store info of filter rule from search text field
        String searchText = searcher.getText();
        RowFilter<TableModel, Object> searchFilter = null;
        if (searchText != null && !searchText.isEmpty()) {
            searchFilter = RowFilter.regexFilter(searchText, 0, 1, 2);
        }

        // combine
        if (buttonFilter == null && searchFilter == null) {
            inventoryTableRowSorter.setRowFilter(null);
        } else if (buttonFilter != null && searchFilter == null) {
            inventoryTableRowSorter.setRowFilter(buttonFilter);
        } else if (buttonFilter == null && searchFilter != null) {
            inventoryTableRowSorter.setRowFilter(searchFilter);
        } else {
            inventoryTableRowSorter.setRowFilter(RowFilter.andFilter(Arrays.asList(buttonFilter, searchFilter)));
        }
    }

    @Override
    public void onceProductAdded(Product product) {
        products.addProduct(product);
        setFileAndTableData();
    }

    @Override
    public void onceProductChanged() {
        setFileAndTableData();
    }

    @Override
    public void onceProductRemoved(Product product) {

    }

    public void setFileAndTableData() {
        FileManager.setProductsList(products);
        FileManager.rewriteFile();
        inventoryTableAdapter.fireTableDataChanged();
    }
}
