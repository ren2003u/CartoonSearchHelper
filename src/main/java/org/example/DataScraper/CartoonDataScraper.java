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
import java.util.*;
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

        Map<String, List<String>> attributes = new HashMap<>();
        for (WebElement h5Element : h5Elements) {
            String attributeKey = h5Element.getText().split("：")[0];
            List<String> attributeValues = getElementsText(h5Element, "a div.no-select");
            attributes.put(attributeKey, attributeValues);
        }

        Document cartoonDoc = new Document();
        cartoonDoc.append("cartoonId", cartoonId)
                .append("transliterationTitle", transliterationTitle)
                .append("japaneseTitle", japaneseTitle);

        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            cartoonDoc.append(entry.getKey(), entry.getValue());
        }

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

