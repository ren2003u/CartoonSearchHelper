package org.example;

import org.example.DataScraper.CartoonDataScraper;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        CartoonDataScraper scraper = new CartoonDataScraper();
        try {
            scraper.scrapeCartoon(93182);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}