import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.pmw.tinylog.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Web scraper for Tredz website.
 */
public class TredzScraper extends WebScraper {

    /**
     * @param scrapeDelay   seconds for delay between scraping
     * @param bikesDao      object for database interaction
     */
    public TredzScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);

    }

    /**
     * Executes first when thread is started and starts scraping.
     * If stop thread has been triggered then scraping stops after finishing.
     * If an exception occurs during scraping then other threads receive the stop state.
     */
    public void run(){
        Logger.info("Tredz scraper starting");
        stop = false;
        while(!stop) {
            try {
                scrape();
            } catch (Exception ex) {
                ex.printStackTrace();
                stop = true;
            }
            Logger.info("Scrape finished. Waiting " + scrapeDelay + " seconds");
            sleep(scrapeDelay);
        }
    }

    /**
     * Scrapes road bikes and comparisons.
     * Iterates through all available pages with road bikes and accesses every bike's page
     * to scrape description, colors, sizes and price.
     * Each found road bike is added to database
     * and separate comparison objects are created for all available colors and sizes.
     * Every color is clicked and then the available sizes in order to update the price.
     *
     * @throws Exception
     */
    void scrape() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.tredz.co.uk/road-bikes");

        int pages = driver.findElements(By.cssSelector(".top .page-number")).size();
        for (int j = 0; j < pages; j++) {
            if (j > 0) {
                WebElement pageButton = driver.findElements(By.cssSelector(".page-number")).get(j);
                pageButton.click();
            }
            String pageURL = driver.getCurrentUrl();

            int gridSize = driver.findElements(By.cssSelector(".search-results a.ga")).size();
            for (int i = 0; i < gridSize; i++) {
                WebElement item = driver.findElements(By.cssSelector(".search-results a.ga")).get(i);
                String url = item.getAttribute("href");
                driver.get(url);

                String name = driver.findElement(By.className("js-product-title")).getText();
                name = name.split(" - ")[0].toUpperCase();
                String imageUrl = driver.findElement(By.id("js-product-image-zoom")).getAttribute("src");
                String description = driver.findElement(By.cssSelector(".description-specification-container__left p")).getText();

                RoadBike roadBike = new RoadBike(shortenName(name), imageUrl, description);
                Logger.info(roadBike);
                bikesDao.addRoadBike(roadBike);

                List<WebElement> colors = driver.findElements(By.className("color"));
                if (colors.size() > 1)
                    for (WebElement color: colors) {
                        jsClickExecutor(driver, color);
                        scrapePrice(driver, color.getText(), roadBike, url, name);
                    }
                else
                    scrapePrice(driver, colors.get(0).getText(), roadBike, url, name);

                driver.get(pageURL);
            }
        }
        driver.close();
    }

    /**
     * Finds price for each available size and adds it to product_comparison table.
     * Clicks on every size to update the price before scraping it.
     *
     * @param driver    loaded product page
     * @param colorName color of the product
     * @param roadBike  object from road_bike table to link the comparison to
     * @param url       product page
     * @param name      original bike name
     */
    private void scrapePrice(WebDriver driver,String colorName, RoadBike roadBike, String url, String name) {
        WebElement sizesDiv = driver.findElement(By.className("sku-option"));
        List<WebElement> sizes = sizesDiv.findElements(By.className("option-label"));

        for (WebElement element: sizes) {
            jsClickExecutor(driver, element);

            String size = element.getText().split("£")[0].split(" ")[0];
            String price = driver.findElement(By.className("value")).getText().split(" ")[0];

            ProductComparison product = new ProductComparison(roadBike, size, shortenColor(colorName), price, url, name);

            bikesDao.addProductComparison(product);
        }
    }

}
