package org.example;

import org.example.DataScraper.CartoonDataScraper;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        CartoonDataScraper scraper = new CartoonDataScraper();
        String[] cartoonIds = new String[5];
        for (int i = 0; i < 5; i++) {
            cartoonIds[i] = String.valueOf(59320 + i);
        }
        scraper.scrapeCartoons(cartoonIds);
    }
}