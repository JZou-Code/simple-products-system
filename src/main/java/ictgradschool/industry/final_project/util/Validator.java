package ictgradschool.industry.final_project.util;

import ictgradschool.industry.final_project.Product;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

public class Validator {
    public static boolean validateInventoryFormat(List<String> productsList) {
        if (!productsList.isEmpty()) {
            HashSet<String> set = new HashSet<>();
            for (String s : productsList) {
                if (validateFormat(s)) return false;
                set.add(s.trim().split(",")[0].trim());
            }
            if(set.size()!=productsList.size()){
                System.out.println("Invalid file. -- integer");
                return false;
            }
        }
        return true;
    }

    public static boolean validateCartInfoFormat(List<String> productsList) {
        if (!productsList.isEmpty()) {
            for (String s : productsList) {
                if (validateFormat(s)) return false;
            }
        }
        return true;
    }

    private static boolean validateFormat(String s) {
        String[] productParts = s.trim().split(",");

        if (productParts.length != 5 || productParts[0].length() != 10) {
            System.out.println("Invalid file. -- length");
            return true;
        }

        try {
            BigDecimal price = new BigDecimal(productParts[3].trim());
            if(price.scale()>2){
                return true;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid file. -- scale");
            return true;
        }

        try {
            int quantity = Integer.parseInt(productParts[4].trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid file. -- decimal");
            return true;
        }
        return false;
    }

    public static boolean validateHeaderFormat(String header) {
        if (header == null || header.isEmpty()) {
            return false;
        }

        if ("inventory-list".equals(header.trim())) {
            // file content invalid at the very beginning if true
            return true;
        }
        System.out.println("Invalid file header.");
        return false;
    }

    public static boolean validateIDFormat(String id, List<Product> productsList) {
        if (!id.matches("^[A-Z0-9]{10}$")) {
            return false;
        }

        for (Product p : productsList) {
            if(p.getId().equals(id)) {
                return false;
            }
        }
        return true;
    }

    public static boolean validatePriceFormat(String price) {
        try{
            if(!price.matches("[0-9]+(\\.[0-9]{1,2})?")){
                return false;
            }
            BigDecimal result = new BigDecimal(price);
            return result.scale() <= 2;
        } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
            return false;
        }
    }

    public static boolean validateQuantityFormat(String quantity) {
        try{
            if(quantity.matches(".*[^0-9].*")){
                return false;
            }
            Integer.parseInt(quantity);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
            return false;
        }
    }
}
