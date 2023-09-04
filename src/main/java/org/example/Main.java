package org.example;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    private static final String ALBERT_URL = "https://akce.najdislevu.cz/albert/";
    private static final String URL = "jdbc:mysql://localhost:3306/transactions";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345";

    private static final ArrayList<Product> listWithAllProducts = new ArrayList<>();

    private static final HashMap<String, String> links = new HashMap<>();

//    private static final String TEMPLATE_OF_THE_TASK_FOR_PUTTING_VALUES = "insert into products (name, price) values (?, ?)";
//    private static final String TEMPLATE_OF_THE_TASK_FOR_GETTING_VALUES = "select price from products where idproducts = ?";


    public static void main(String[] args) {

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            gettingAllLinksWithCategories(ALBERT_URL);
            links.forEach((String category, String link) -> allTasks(link, connection, category));
//            allTasks(URL_TEMPLATE_FOR_JSOUP_ALBERT, connection, albertList);
//            allTasks(URL_TEMPLATE_FOR_JSOUP_BILLA, connection, billaList);
//            System.out.println("\nAlbert: ");
//            albertList.forEach((price, name) -> System.out.println("Product name: " + name + "\nPrice: " + price + "\n"));
//            System.out.println("\nBilla: ");
//            billaList.forEach((price, name) -> System.out.println("Product name: " + name + "\nPrice: " + price + "\n"));

            listWithAllProducts.forEach(product -> System.out.println(String.format("Product name: %s\nAn old price: %.2f\nPrice with discount: %.2f\nA category: %s\n\n",
                    product.getName(), product.getSimplePrice(), product.getDiscountPrice(), product.getCategory())));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void allTasks(String link, Connection connection, String category) {
        try {
            var document = Jsoup.connect(link).get();
            var elements = document.select("div.acProd");
//            PreparedStatement puttingStatement = connection.prepareStatement(TEMPLATE_OF_THE_TASK_FOR_PUTTING_VALUES);

            String title = document.select("h3").text();
            System.out.println(title);
            elements.forEach(i ->  {
                String productName = i.select("a").attr("title");

                String cleanedNewPricePhrase = ((i.select("div.acNewPr").textNodes().get(0)).text()).replaceAll("[^0-9,]", "");
                String cleanedOldPricePhrase = ((i.select("div.acNewPr span").text()).replaceAll("[^0-9,]", ""));

                if (!cleanedNewPricePhrase.isEmpty() && !cleanedOldPricePhrase.isEmpty()) {
                    String newPricePhrase = cleanedNewPricePhrase.replace(",", ".");
                    String oldPricePhrase = cleanedOldPricePhrase.replace(",", ".");

                    BigDecimal newPrice = new BigDecimal(newPricePhrase);
                    BigDecimal oldPrice = new BigDecimal(oldPricePhrase);
                    listWithAllProducts.add(new Product(productName, oldPrice, newPrice, category));
                } else {
                    // Handle the case where price information is missing or invalid
                    System.out.println("Skipping product without valid price: " + productName + "\nLink: " + link + "\n");
                }
//                int rowsAffected;
//                try {
//                    puttingStatement.setString(1, productName);
//                    puttingStatement.setBigDecimal(2, price);
//                    rowsAffected = puttingStatement.executeUpdate();
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//                if(rowsAffected > 0) {
//                    System.out.println("Success in inserting values");
//                } else {
//                    System.out.println("Something went wrong in inserting values");
//                }
//                System.out.println();
            });
//            PreparedStatement gettingStatement = connection.prepareStatement(TEMPLATE_OF_THE_TASK_FOR_GETTING_VALUES);
//            gettingStatement.setInt(1, 2);
//            ResultSet resultSet = gettingStatement.executeQuery();
//            if(resultSet.next()) {
//                System.out.println("Test for checking how works decimal(10,0) in mySql: " + resultSet.getBigDecimal(1));
//            } else {
//                System.out.println("Something went wrong with resultSet");
//            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void gettingAllLinksWithCategories(String shopLink) throws IOException {
        var document = Jsoup.connect(shopLink).get();
        var elements = document.select("div.choosCo a");
        elements.forEach(link -> links.put(link.text(), link.attr("href")));
    }


}