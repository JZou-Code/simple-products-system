package ictgradschool.industry.final_project;

import java.util.ArrayList;
import java.util.List;

public class ProductsList {
    private List<Product> products;
    private List<ProductListListener> listeners;
    private boolean isNotifyEnabled;

    public ProductsList() {
//        this.products = products;
        this.products = new ArrayList<>();
        listeners = new ArrayList<ProductListListener>();
        isNotifyEnabled = false;
    }

    public List<Product> getProductsList() {
        return products;
    }

    public void setProductsList(List<Product> products) {
        this.products = products;
    }

    public void addListener(ProductListListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProductListListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        if (isNotifyEnabled) {
            for (ProductListListener listener : listeners) {
                listener.onceProductChanged();
            }
        }
    }

    public void addProduct(Product product) {
        products.add(product);
        notifyListeners();
    }

    public void removeProduct(int Index) {
        products.remove(Index);
        notifyListeners();
    }

    public void removeAllProducts() {
        products.clear();
    }

    public void setSingleProduct(Product product, int index) {
        products.set(index, product);
        notifyListeners();
    }

    public boolean isNotifyEnabled() {
        return isNotifyEnabled;
    }

    public void setNotifyEnabled(boolean notifyEnabled) {
        isNotifyEnabled = notifyEnabled;
    }
}
