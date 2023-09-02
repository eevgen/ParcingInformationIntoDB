package org.example;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class Main {

    private static final String URL_FOR_JSOUP = "https://akce.najdislevu.cz/albert/?strana=0";
    private static final String URL = "jdbc:mysql://localhost:3306/transactions";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345";
    private static final String TEMPLATE_OF_THE_TASK_FOR_PUTTING_VALUES = "insert into products (name, price) values (?, ?)";
    private static final String TEMPLATE_OF_THE_TASK_FOR_GETTING_VALUES = "select price from products where idproducts = ?";


    public static void main(String[] args) {
        try {
            var document = Jsoup.connect(URL_FOR_JSOUP).get();
            var elements = document.select("div.acProd");

            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            PreparedStatement puttingStatement = connection.prepareStatement(TEMPLATE_OF_THE_TASK_FOR_PUTTING_VALUES);

            String title = document.select("h3").text();
            System.out.println(title);
            elements.forEach(i ->  {
                String cleanedPhrase = ((i.select("div.acNewPr").textNodes().get(0)).text()).replaceAll("[^0-9,]", "");
                String replacePointsFromCleanedPhrase = cleanedPhrase.replace(",", ".");
                BigDecimal price = new BigDecimal(replacePointsFromCleanedPhrase);
                String productName = i.select("a").attr("title");
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