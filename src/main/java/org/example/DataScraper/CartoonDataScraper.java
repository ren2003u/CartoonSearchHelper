package org.example.DataScraper;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CartoonDataScraper {
    private WebDriver driver;

    public CartoonDataScraper() {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver_win32\\chromedriver.exe");
        this.driver = new ChromeDriver();
    }

    public void scrapeCartoons(String[] cartoonIds) {
        // Connect to MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("cartoonsDB");
        MongoCollection<Document> collection = database.getCollection("cartoons");

        for (String cartoonId : cartoonIds) {
            scrapeCartoon(cartoonId, collection);
        }

        driver.quit();
    }

    private void scrapeCartoon(String cartoonId, MongoCollection<Document> collection) {
        driver.get("https://hanime1.me/comic/" + cartoonId);

        WebElement cartoon = driver.findElement(By.className("col-md-8"));

        String transliterationTitle = getAttributeOrDefault(cartoon, "h3.title span.pretty", "No transliteration title");
        String japaneseTitle = getAttributeOrDefault(cartoon, "h4.title span.pretty", "No Japanese title");

        List<WebElement> h5Elements = cartoon.findElements(By.cssSelector("div.comics-metadata-margin-top h5"));

        List<String> tags = h5Elements.size() > 0 ? getElementsText(h5Elements.get(0), "a div.no-select") : new ArrayList<>();
        List<String> authors = h5Elements.size() > 1 ? getElementsText(h5Elements.get(1), "a div.no-select") : new ArrayList<>();
        List<String> languages = h5Elements.size() > 2 ? getElementsText(h5Elements.get(2), "a div.no-select") : new ArrayList<>();
        List<String> categories = h5Elements.size() > 3 ? getElementsText(h5Elements.get(3), "a div.no-select") : new ArrayList<>();
        String pageCount = h5Elements.size() > 4 ? h5Elements.get(4).getText().replace("頁數：", "").trim() : "No page count";

        Document cartoonDoc = new Document();
        cartoonDoc.append("cartoonId", cartoonId)
                .append("transliterationTitle", transliterationTitle)
                .append("japaneseTitle", japaneseTitle)
                .append("tags", tags)
                .append("author", authors)
                .append("languages", languages)
                .append("categories", categories)
                .append("pageCount", pageCount);

        collection.insertOne(cartoonDoc);
    }

    private String getAttributeOrDefault(WebElement parent, String cssSelector, String defaultValue) {
        try {
            return parent.findElement(By.cssSelector(cssSelector)).getText();
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    private List<String> getElementsText(WebElement parent, String cssSelector) {
        List<String> elementsText = new ArrayList<>();
        List<WebElement> elements = parent.findElements(By.cssSelector(cssSelector));

        for (WebElement element : elements) {
            elementsText.add(element.getText());
        }

        return elementsText;
    }
}

