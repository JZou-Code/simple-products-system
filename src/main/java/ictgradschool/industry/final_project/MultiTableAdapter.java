package ictgradschool.industry.final_project;

import ictgradschool.industry.final_project.util.FileManager;
import ictgradschool.industry.final_project.util.Validator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MultiTableAdapter extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Name", "Description", "Price", "Quantity"};
    ProductsList products;
    boolean isEditable = false;

    public void setProducts(ProductsList products) {
        this.products = products;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public MultiTableAdapter(ProductsList productsList) {
        this.products = productsList;
    }

    @Override
    public int getRowCount() {
        return products.getProductsList().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product product = products.getProductsList().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return product.getId();
            case 1:
                return product.getName();
            case 2:
                return product.getDescription();
            case 3:
                return product.getPrice();
            case 4:
                return product.getQuantity();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isEditable;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Product product = products.getProductsList().get(rowIndex);
        String content = aValue.toString().trim();
        switch (columnIndex) {
            case 0:
                String oleId = product.getId();
                if(oleId.equals(content)) {
                    return;
                }
                if(!Validator.validateIDFormat(content,products.getProductsList())){
                    JOptionPane.showMessageDialog(null, "The ID must be a unique 10-character identifier, consisting only of numbers and uppercase letters.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                };
                product.setId(content);
                break;
            case 1:
                product.setName(content);
                break;
            case 2:
                product.setDescription(content);
                break;
            case 3:
                if(!Validator.validatePriceFormat(content)){
                    JOptionPane.showMessageDialog(null, "Price must be a non-negative number with up to two decimal places.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                };
                product.setPrice(new BigDecimal(content).setScale(2, RoundingMode.HALF_UP));
                break;
            case 4:
                if(!Validator.validateQuantityFormat(content)){
                    JOptionPane.showMessageDialog(null, "Quantity must be a non-negative Integer.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                };
                 product.setQuantity(Integer.parseInt(content));
        }

        FileManager.setProductsList(products);
        fireTableDataChanged();
        FileManager.rewriteFile();
    }
}
