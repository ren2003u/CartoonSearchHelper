package org.example.DataScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CartoonDataScraper {
    public void scrapeCartoon(int id) throws IOException {
        String url = "https://hanime1.me/comic/" + id;
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36")
                .get();

        String transliterationTitle = doc.select("h3.title span.pretty").text();
        String japaneseTitle = doc.select("h4.title span.pretty").text();

        Elements tagElements = doc.select("div.comics-metadata-margin-top h5:contains(標籤) a");
        List<String> tags = tagElements.eachText();

        Elements authorElements = doc.select("h5:contains(作者) a");
        List<String> authors = authorElements.eachText();

        Elements languageElements = doc.select("h5:contains(語言) a");
        List<String> languages = languageElements.eachText();

        String category = doc.select("h5:contains(分類) a").text();
        String pages = doc.select("h5:contains(頁數) div").text();

        // For now, just print the information
        System.out.println("Transliteration Title: " + transliterationTitle);
        System.out.println("Japanese Title: " + japaneseTitle);
        System.out.println("Tags: " + tags);
        System.out.println("Authors: " + authors);
        System.out.println("Languages: " + languages);
        System.out.println("Category: " + category);
        System.out.println("Pages: " + pages);
    }
}
