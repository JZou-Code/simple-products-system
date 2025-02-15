package ictgradschool.industry.final_project;

import java.math.BigDecimal;
import java.util.List;

public class Receipt {
    private List<Product> products;
    private BigDecimal totalPrice;

    public Receipt(ProductsList productsList) {
        this.products = productsList.getProductsList();
        this.totalPrice = calculateTotalPrice();
    }

    public BigDecimal calculateTotalPrice() {
        BigDecimal tempTotal = new BigDecimal(0);
        for (Product product : products) {
            tempTotal = tempTotal.add(product.getPrice().multiply(new BigDecimal(product.getQuantity())));
        }
        return tempTotal;
    }

    @Override
    public String toString() {
        BigDecimal totalPrice = new BigDecimal(0);
        String totalPriceString = "";
        StringBuilder newReceipt = new StringBuilder();
        newReceipt.append("-----------Thank You For Shopping----------\n");
        newReceipt.append("-------------------------------------------\n");
        for (Product product : products) {
//            System.out.println("Receipt product = " + product);
            String unitPrice = "";
            BigDecimal itemPrice = new BigDecimal(0);
            BigDecimal price = product.getPrice();
            int quantity = product.getQuantity();

            if (quantity > 1) {
                unitPrice = String.format("($%.2f)", price);
            }

            itemPrice = price.multiply(new BigDecimal(quantity));
            totalPrice = totalPrice.add(itemPrice);
            String temp = "";

            String itemStrInfo = String.format("%4d %-1s %-14s %10s %9s\n",
                    product.getQuantity(),
                    temp,
                    product.getName(),
                    unitPrice,
                    String.format("$%.2f", itemPrice));

            newReceipt.append(itemStrInfo);
        }
        String temp = "TOTAL";
        totalPriceString = String.format("%7s%-5s%21s%9s\n", "", temp, "", String.format("$%.2f", totalPrice));
        newReceipt.append("===========================================\n");
        newReceipt.append(totalPriceString);
        newReceipt.append("-------------------------------------------\n");

        return newReceipt.toString();
    }
}
