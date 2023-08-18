import org.example.DataScraper.CartoonDataScraper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CartoonDataScraperTest {

    @Autowired
    private CartoonDataScraper cartoonDataScraper;

    @Test
    public void testScrapeCartoons() {
            CartoonDataScraper scraper = new CartoonDataScraper();
            String[] cartoonIds = new String[10];
            for (int i = 0; i < 10; i++) {
                cartoonIds[i] = String.valueOf(12347 + i);
            }
            scraper.scrapeCartoons(cartoonIds);
    }
}
