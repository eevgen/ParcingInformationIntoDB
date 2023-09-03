package org.example;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;

public class Main {

    private static final String URL_FOR_JSOUP_ALBERT = "https://akce.najdislevu.cz/albert/?strana=0";
    private static final String URL_FOR_JSOUP_BILLA = "https://akce.najdislevu.cz/billa/";
    private static final String URL = "jdbc:mysql://localhost:3306/transactions";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345";

    private static final HashMap<BigDecimal, String> albertList = new HashMap<>();
    private static final HashMap<BigDecimal, String> billaList = new HashMap<>();

    private static final String TEMPLATE_OF_THE_TASK_FOR_PUTTING_VALUES = "insert into products (name, price) values (?, ?)";
    private static final String TEMPLATE_OF_THE_TASK_FOR_GETTING_VALUES = "select price from products where idproducts = ?";


    public static void main(String[] args) {

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            allTasks(URL_FOR_JSOUP_ALBERT, connection, albertList);
            allTasks(URL_FOR_JSOUP_BILLA, connection, billaList);
            System.out.println("\nAlbert: ");
            albertList.forEach((price, name) -> System.out.println("Product name: " + name + "\nPrice: " + price + "\n"));
            System.out.println("\nBilla: ");
            billaList.forEach((price, name) -> System.out.println("Product name: " + name + "\nPrice: " + price + "\n"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void allTasks(String link, Connection connection, HashMap<BigDecimal, String> list) {
        try {
            var document = Jsoup.connect(link).get();
            var elements = document.select("div.acProd");
            PreparedStatement puttingStatement = connection.prepareStatement(TEMPLATE_OF_THE_TASK_FOR_PUTTING_VALUES);

            String title = document.select("h3").text();
            System.out.println(title);
            elements.forEach(i ->  {
                String cleanedPhrase = ((i.select("div.acNewPr").textNodes().get(0)).text()).replaceAll("[^0-9,]", "");
                String replacePointsFromCleanedPhrase = cleanedPhrase.replace(",", ".");
                BigDecimal price = new BigDecimal(replacePointsFromCleanedPhrase);
                String productName = i.select("a").attr("title");
                list.put(price, productName);
                int rowsAffected;
                try {
                    puttingStatement.setString(1, productName);
                    puttingStatement.setBigDecimal(2, price);
                    rowsAffected = puttingStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if(rowsAffected > 0) {
                    System.out.println("Success in inserting values");
                } else {
                    System.out.println("Something went wrong in inserting values");
                }
                System.out.println();
            });
            PreparedStatement gettingStatement = connection.prepareStatement(TEMPLATE_OF_THE_TASK_FOR_GETTING_VALUES);
            gettingStatement.setInt(1, 2);
            ResultSet resultSet = gettingStatement.executeQuery();
            if(resultSet.next()) {
                System.out.println("Test for checking how works decimal(10,0) in mySql: " + resultSet.getBigDecimal(1));
            } else {
                System.out.println("Something went wrong with resultSet");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}