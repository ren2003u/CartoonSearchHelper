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



/**
 * CartoonDataScraper is responsible for scraping cartoon data from a specific website and storing it in a MongoDB collection.
 */
public class CartoonDataScraper {

    /**
     * Initiates the scraping process for multiple cartoon IDs.
     *
     * @param cartoonIds Array of cartoon IDs to be scraped.
     */
    public void scrapeCartoons(String[] cartoonIds) {
        // Establish a connection to the MongoDB server.
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        // Access the database named "cartoonsDB".
        MongoDatabase database = mongoClient.getDatabase("cartoonsDB");
        // Access the collection named "cartoons" within the database.
        MongoCollection<Document> collection = database.getCollection("cartoons");

        // Iterate over each cartoon ID and scrape its data.
        for (String cartoonId : cartoonIds) {
            scrapeCartoon(cartoonId, collection);
        }
    }

    /**
     * Scrapes data for a single cartoon based on its ID and stores it in the provided MongoDB collection.
     *
     * @param cartoonId   The ID of the cartoon to be scraped.
     * @param collection  The MongoDB collection where the scraped data will be stored.
     */
    private void scrapeCartoon(String cartoonId, MongoCollection<Document> collection) {
        // Set the path for the ChromeDriver.
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver_win32\\chromedriver.exe");
        // Initialize a new ChromeDriver instance.
        WebDriver driver = new ChromeDriver();

        // Navigate to the cartoon's URL.
        driver.get("https://hanime1.me/comic/" + cartoonId);

        // Find the main cartoon element on the page.
        WebElement cartoon = driver.findElement(By.className("col-md-8"));

        // Extract the transliteration title, Japanese title, and image URL.
        String transliterationTitle = getAttributeOrDefault(cartoon, "h3.title span.pretty", "No transliteration title");
        String japaneseTitle = getAttributeOrDefault(cartoon, "h4.title span.pretty", "No Japanese title");
        String imageUrl = driver.findElement(By.cssSelector(".col-md-4 img")).getAttribute("src");

        // Extract additional attributes associated with the cartoon.
        List<WebElement> h5Elements = cartoon.findElements(By.cssSelector("div.comics-metadata-margin-top h5"));
        Map<String, List<String>> attributes = new HashMap<>();
        for (WebElement h5Element : h5Elements) {
            String attributeKey = h5Element.getText().split("：")[0];
            List<String> attributeValues = getElementsText(h5Element, "a div.no-select");
            attributes.put(attributeKey, attributeValues);
        }

        // Attempt to scrape the page number, if available.
        try {
            WebElement pageNumberElement = cartoon.findElement(By.xpath(".//h5[contains(text(),'頁數')]/div"));
            String pageNumber = pageNumberElement.getText();
            attributes.put("頁數", Collections.singletonList(pageNumber));
        } catch (NoSuchElementException e) {
            System.out.println("Page number not found for cartoonId: " + cartoonId);
        }

        // Construct a MongoDB document with the scraped data.
        Document cartoonDoc = new Document();
        cartoonDoc.append("cartoonId", cartoonId)
                .append("transliterationTitle", transliterationTitle)
                .append("japaneseTitle", japaneseTitle)
                .append("imageUrl", imageUrl);

        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            cartoonDoc.append(entry.getKey(), entry.getValue());
        }

        // Insert the constructed document into the MongoDB collection.
        collection.insertOne(cartoonDoc);

        // Close the browser window and terminate the WebDriver session.
        driver.quit();
    }

    /**
     * Attempts to retrieve an attribute's value using a CSS selector. If not found, returns a default value.
     *
     * @param parent       The parent WebElement to search within.
     * @param cssSelector  The CSS selector to locate the desired element.
     * @param defaultValue The default value to return if the element is not found.
     * @return The text of the found element or the default value.
     */
    private String getAttributeOrDefault(WebElement parent, String cssSelector, String defaultValue) {
        try {
            return parent.findElement(By.cssSelector(cssSelector)).getText();
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the text of multiple elements located within a parent element using a CSS selector.
     *
     * @param parent      The parent WebElement to search within.
     * @param cssSelector The CSS selector to locate the desired elements.
     * @return A list of text values from the found elements.
     */
    private List<String> getElementsText(WebElement parent, String cssSelector) {
        List<String> elementsText = new ArrayList<>();
        List<WebElement> elements = parent.findElements(By.cssSelector(cssSelector));

        for (WebElement element : elements) {
            elementsText.add(element.getText());
        }

        return elementsText;
    }
}


