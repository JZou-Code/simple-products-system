package ictgradschool.industry.final_project;

import java.util.List;

public interface ProductListListener {
    void onceProductAdded(Product product);

    void onceProductChanged();

    void onceProductRemoved(Product product);
}
