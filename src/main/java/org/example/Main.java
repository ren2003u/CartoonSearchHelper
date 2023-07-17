package org.example;

import org.example.DataScraper.CartoonDataScraper;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        CartoonDataScraper scraper = new CartoonDataScraper();
        String[] cartoonIds = new String[11];
        for (int i = 0; i < 11; i++) {
            cartoonIds[i] = String.valueOf(93172 + i);
        }
        scraper.scrapeCartoons(cartoonIds);
    }
}