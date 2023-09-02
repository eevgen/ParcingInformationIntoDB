package org.example;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.math.BigDecimal;

public class Main {

    private static final String URL_FOR_JSOUP = "https://akce.najdislevu.cz/albert/?strana=0";

    public static void main(String[] args) {
        try {
            var document = Jsoup.connect(URL_FOR_JSOUP).get();
            var elements = document.select("div.acProd");
            elements.forEach(i ->  {
                String cleanedPhrase = ((i.select("div.acNewPr").textNodes().get(0)).text()).replaceAll("[^0-9,]", "");
                String replacePointsFromCleanedPhrase = cleanedPhrase.replace(",", ".");
                BigDecimal price = new BigDecimal(replacePointsFromCleanedPhrase);
                System.out.println("Product name: " + i.select("a").attr("title") +
                        "\nPrice: " + price + "\n");

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}