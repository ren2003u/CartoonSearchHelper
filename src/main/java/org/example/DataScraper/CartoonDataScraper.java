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
import java.util.List;
import java.util.stream.Collectors;

public class CartoonDataScraper {
    private WebDriver driver;

    public CartoonDataScraper() {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver_win32\\chromedriver.exe");
        this.driver = new ChromeDriver();
    }
//    public void scrapeCartoon(int id) throws IOException {
//        String url = "https://hanime1.me/comic/" + id;
//        Document doc = Jsoup.connect(url)
//                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36")
//                .get();
//
//        String transliterationTitle = doc.select("h3.title span.pretty").text();
//        String japaneseTitle = doc.select("h4.title span.pretty").text();
//
//        Elements tagElements = doc.select("div.comics-metadata-margin-top h5:contains(標籤) a");
//        List<String> tags = tagElements.eachText();
//
//        Elements authorElements = doc.select("h5:contains(作者) a");
//        List<String> authors = authorElements.eachText();
//
//        Elements languageElements = doc.select("h5:contains(語言) a");
//        List<String> languages = languageElements.eachText();
//
//        String category = doc.select("h5:contains(分類) a").text();
//        String pages = doc.select("h5:contains(頁數) div").text();
//
//        // For now, just print the information
//        System.out.println("Transliteration Title: " + transliterationTitle);
//        System.out.println("Japanese Title: " + japaneseTitle);
//        System.out.println("Tags: " + tags);
//        System.out.println("Authors: " + authors);
//        System.out.println("Languages: " + languages);
//        System.out.println("Category: " + category);
//        System.out.println("Pages: " + pages);
//    }
    public void scrapeCartoon() {
        driver.get("https://hanime1.me/comic/93182");

        // Connect to MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("cartoonsDB");
        MongoCollection<Document> collection = database.getCollection("cartoons");

        WebElement cartoon = driver.findElement(By.className("col-md-8"));

        String transliterationTitle = getAttributeOrDefault(cartoon, "h3.title span.pretty", "No transliteration title");
        String japaneseTitle = getAttributeOrDefault(cartoon, "h4.title span.pretty", "No Japanese title");

        List<WebElement> h5Elements = cartoon.findElements(By.cssSelector("div.comics-metadata-margin-top h5"));

        String tags = h5Elements.size() > 0 ? h5Elements.get(0).getText().replace("標籤：", "").trim() : "No tags";
        String author = h5Elements.size() > 1 ? h5Elements.get(1).getText().replace("作者：", "").trim() : "No author";
        String languages = h5Elements.size() > 2 ? h5Elements.get(2).getText().replace("語言：", "").trim() : "No languages";
        String categories = h5Elements.size() > 3 ? h5Elements.get(3).getText().replace("分類：", "").trim() : "No categories";
        String pageCount = h5Elements.size() > 4 ? h5Elements.get(4).getText().replace("頁數：", "").trim() : "No page count";

        Document cartoonDoc = new Document();
        cartoonDoc.append("transliterationTitle", transliterationTitle)
                .append("japaneseTitle", japaneseTitle)
                .append("tags", tags)
                .append("author", author)
                .append("languages", languages)
                .append("categories", categories)
                .append("pageCount", pageCount);


        collection.insertOne(cartoonDoc);

        driver.quit();
    }
    private String getAttributeOrDefault(WebElement parent, String cssSelector, String defaultValue) {
        try {
            return parent.findElement(By.cssSelector(cssSelector)).getText();
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }
}
