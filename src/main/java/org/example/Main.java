package org.example;

import org.example.DataScraper.CartoonDataScraper;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        CartoonDataScraper scraper = new CartoonDataScraper();
        String[] cartoonIds = new String[1];
        for (int i = 0; i < 1; i++) {
            cartoonIds[i] = String.valueOf(90107 + i);
        }
        scraper.scrapeCartoons(cartoonIds);
    }
}