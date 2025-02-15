package ictgradschool.industry.final_project.util;

import ictgradschool.industry.final_project.Product;
import ictgradschool.industry.final_project.ProductsList;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;

public class FileManager {
    private static String filePath;
    private static String receiptFilePath;
    private static List<String> filteredProductsStrings;
    private static ProductsList productsList = new ProductsList();
    private static ProductsList cartProductsList = new ProductsList();

    public static void setProductsList(ProductsList productsList) {
        FileManager.productsList = productsList;
    }

    public static void setCartProductsList(ProductsList cartProductsList) {
        FileManager.cartProductsList = cartProductsList;
    }

    public static ProductsList getProductsList() {
        return productsList;
    }

    public static ProductsList getCartProductsList() {
        return cartProductsList;
    }

    public static List<String> getFilteredProductsStrings() {
        return filteredProductsStrings;
    }

    public static void setFilteredProductsStrings(List<String> filteredProductsStrings) {
        FileManager.filteredProductsStrings = filteredProductsStrings;
    }

    public static String getFilePath() {
        return filePath;
    }

    public static void setFilePath(String filePath) {
        FileManager.filePath = filePath;
    }

    public static String getReceiptFilePath() {
        return receiptFilePath;
    }

    public static void setReceiptFilePath(String receiptFilePath) {
        FileManager.receiptFilePath = receiptFilePath;
    }

    public static ProductsList getCurrentInventoryContent() {
        return loadFile()[0];
    }


    private static ProductsList[] loadFile() {
        ProductsList inventory = new ProductsList();
        ProductsList cart = new ProductsList();
        ProductsList[] wrongMessage = {null, null};
        try (DataInputStream in = new DataInputStream(new FileInputStream(filePath))) {
            String head = in.readUTF();

            if (!head.equals("inventory-list")) {
                System.out.println("invalid inventory");
                throw new RuntimeException("Invalid file format");
            }

            int numProducts = in.readInt();

            for (int i = 0; i < numProducts; i++) {
                String id = in.readUTF();
                String name = in.readUTF();
                String description = in.readUTF();

                BigDecimal price = new BigDecimal(in.readUTF());
                int quantity = in.readInt();

                inventory.addProduct(new Product(id, name, description, price, quantity));
            }

            head = in.readUTF();

            if (!head.equals("@cart")) {
                System.out.println("invalid cart");
                throw new RuntimeException("Invalid file format");
            }

            int cartNum = in.readInt();

            for (int i = 0; i < cartNum; i++) {
                String id = in.readUTF();
                String name = in.readUTF();
                String description = in.readUTF();

                BigDecimal price = new BigDecimal(in.readUTF());
                int quantity = in.readInt();

                cart.addProduct(new Product(id, name, description, price, quantity));
            }

        } catch (IOException e) {
            return wrongMessage;
        }

        return new ProductsList[]{inventory, cart};
    }


    public static ProductsList getCurrentCartContent() {
        return loadFile()[1];
    }

    public static void rewriteFile() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filePath))) {
            out.writeUTF("inventory-list");
            out.writeInt(productsList.getProductsList().size());
            for (Product p : productsList.getProductsList()) {
                out.writeUTF(p.getId());
                out.writeUTF(p.getName());
                out.writeUTF(p.getDescription());
                out.writeUTF(p.getPrice().toString());
                out.writeInt(p.getQuantity());
            }
            out.writeUTF("@cart");
            out.writeInt(cartProductsList.getProductsList().size());
            for (Product p : cartProductsList.getProductsList()) {
                out.writeUTF(p.getId());
                out.writeUTF(p.getName());
                out.writeUTF(p.getDescription());
                out.writeUTF(p.getPrice().toString());
                out.writeInt(p.getQuantity());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
