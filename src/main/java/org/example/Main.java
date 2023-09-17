package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class Main {

    private static final String ALL_SHOPS_URLS = "https://akce.najdislevu.cz/";
    private static final String URL = "jdbc:mysql://localhost:3306/transactions";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345";

    private static final ArrayList<Product> listWithAllProducts = new ArrayList<>();
    private static final HashSet<String> categories = new HashSet<>();

    private static final HashMap<String, String> links = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);

    private static final String TEMPLATE_OF_THE_TASK_FOR_PUTTING_VALUES = "insert into products (name, oldprice, newprice, category, supermarket, link) values (?, ?, ?, ?, ?, ?)";
//    private static final String TEMPLATE_OF_THE_TASK_FOR_GETTING_VALUES = "select price from products where idproducts = ?";


    public static void main(String[] args) {

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {

//            allTasks(URL_TEMPLATE_FOR_JSOUP_ALBERT, connection, albertList);
//            allTasks(URL_TEMPLATE_FOR_JSOUP_BILLA, connection, billaList);
//            System.out.println("\nAlbert: ");
//            albertList.forEach((price, name) -> System.out.println("Product name: " + name + "\nPrice: " + price + "\n"));
//            System.out.println("\nBilla: ");
//            billaList.forEach((price, name) -> System.out.println("Product name: " + name + "\nPrice: " + price + "\n"));
            System.out.print("Enter please a category name: ");
            String phrase = scanner.nextLine();
            System.out.print("Enter please a supermarket name: ");
            String supermarket = scanner.nextLine();
            gettingAllLinksFromAllShopsWithCategorie(ALL_SHOPS_URLS, phrase, supermarket);
            links.forEach((String categoryWithLink, String link) -> allTasks(link, connection, categoryWithLink, supermarket));
            listWithAllProducts.forEach(product -> {
                categories.forEach(category -> {
                    if (phrase.equalsIgnoreCase(category) && product.getCategory().equalsIgnoreCase(category) && supermarket.equalsIgnoreCase(product.getSupermarket())) {
                        System.out.println(String.format("Product name: %s\nAn old price: %.2f\nPrice with discount: %.2f\nA category: %s\nA link: %s\nA supermarket: %s\n\n",
                                product.getName(), product.getSimplePrice(), product.getDiscountPrice(), product.getCategory(), product.getLink(), product.getSupermarket()));
                    }
                });
            });
            gettingProductsWithPriceSorter(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void allTasks(String link, Connection connection, String category, String usersSupermarket) {
        Document document;
        Elements elements;
            try {
                document = Jsoup.connect(link).get();
                elements = document.select("div.acProd");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            elements.forEach(i -> {
                PreparedStatement puttingStatement = null;
                Statement checkingStatement = null;
                try {
                    puttingStatement = connection.prepareStatement(TEMPLATE_OF_THE_TASK_FOR_PUTTING_VALUES);
                    checkingStatement = connection.createStatement();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                String productName = i.select("a").attr("title");
                String supermarket = null;
                Element divElement = i.select("div.acPdEx.center").first();
                if (divElement != null) {
                    Element imgElement = divElement.select("a").first();
                    if (imgElement != null) {
                        supermarket = imgElement.text();
                    } else {
                        System.out.println("img element not found inside div.acPdEx.center");
                    }
                } else {
                    System.out.println("div.acPdEx.center element not found");
                }
                if (usersSupermarket.equalsIgnoreCase(supermarket)) {
                    String cleanedNewPricePhrase = ((i.select("div.acNewPr").textNodes().get(0)).text()).replaceAll("[^0-9,]", "");
                    String cleanedOldPricePhrase = ((i.select("div.acNewPr span").text()).replaceAll("[^0-9,]", ""));

                    BigDecimal oldPrice = null;
                    BigDecimal newPrice = null;

                    if (!cleanedNewPricePhrase.isEmpty() && !cleanedOldPricePhrase.isEmpty()) {
                        String newPricePhrase = cleanedNewPricePhrase.replace(",", ".");
                        String oldPricePhrase = cleanedOldPricePhrase.replace(",", ".");

                        newPrice = new BigDecimal(newPricePhrase);
                        oldPrice = new BigDecimal(oldPricePhrase);

                        categories.add(category);
                        listWithAllProducts.add(new Product(productName, oldPrice, newPrice, category, link, supermarket));
                    }
                    int rowsAffected = 0;
                    boolean trueOrFalse;

                    Statement statement;
                    ResultSet emptyOrNotResultSet;
                    int rowCount;
                    try {
                        String query = "SELECT * FROM products WHERE name = ? AND oldprice = ? AND newprice = ? AND category = ? AND supermarket = ? AND link = ?";
                        PreparedStatement checkStatement = connection.prepareStatement(query);
                        checkStatement.setString(1, productName);
                        checkStatement.setFloat(2, oldPrice.floatValue());
                        checkStatement.setFloat(3, newPrice.floatValue());
                        checkStatement.setString(4, category);
                        checkStatement.setString(5, supermarket);
                        checkStatement.setString(6, link);
                        ResultSet resultSet = checkStatement.executeQuery();
                        trueOrFalse = resultSet.next();

                        statement = connection.createStatement();
                        emptyOrNotResultSet = statement.executeQuery("SELECT COUNT(*) FROM products");
                        emptyOrNotResultSet.next();
                        rowCount = emptyOrNotResultSet.getInt(1);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    if(trueOrFalse || rowCount == 0) {
                        try {
                            puttingStatement.setString(1, productName);
                            puttingStatement.setFloat(2, oldPrice.floatValue());
                            puttingStatement.setFloat(3, newPrice.floatValue());
                            puttingStatement.setString(4, category);
                            puttingStatement.setString(5, supermarket);
                            puttingStatement.setString(6, link);
                            rowsAffected = puttingStatement.executeUpdate();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        if (rowsAffected > 0) {
                            System.out.println("Success in inserting values");
                        } else {
                            System.out.println("Something went wrong in inserting values");
                        }
                    }
                    System.out.println();
                }
            });
    }

    public static void gettingAllLinksWithCategories(final ArrayList<String> shoplinks) throws IOException {
        for (String shoplink : shoplinks) {
            var document = Jsoup.connect(shoplink).get();
            var elements = document.select("div.choosCo a");
            elements.forEach(link -> links.put(link.text(), link.attr("href")));
        }
    }

    public static void gettingAllLinksFromAllShopsWithCategorie(final String URL, String usersCategory, String supermarket) {
        Document document;
        try {
            document = Jsoup.connect(URL).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements elements = document.select("div.titCat");
        elements.forEach(biggerCategory -> {
            Elements elementsSmall = biggerCategory.select("div.catUn a");
            elementsSmall.forEach(smallerCategory -> {
                    if (usersCategory.equals(smallerCategory.text())) {
                        links.put(smallerCategory.text(), smallerCategory.attr("href"));
                    }
            });
        });
    }

    public static void gettingProductsWithPriceSorter(Connection connection) {
        ArrayList<BigDecimal> listWithPrices = new ArrayList<>();
        ResultSet allNewPrices = getMySQLProducts("select newprice from products;", connection);
        listWithAllProducts.forEach(product -> {
            try {
                if(allNewPrices.next()) {
                    BigDecimal newPrice = allNewPrices.getBigDecimal("newprice");
                    listWithPrices.add(newPrice);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        BigDecimal[] prices = listWithPrices.toArray(new BigDecimal[0]);
        BigDecimal[] listOfPricesInOrder = findHighestNumber(prices);
        for (BigDecimal bigDecimal : listOfPricesInOrder) {
            System.out.println(bigDecimal);
        }
    }

    public static ResultSet getMySQLProducts(String query, Connection connection) {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static BigDecimal[] findHighestNumber(BigDecimal[] prices) {
        Arrays.sort(prices, Comparator.reverseOrder());
        return prices;
    }

}
