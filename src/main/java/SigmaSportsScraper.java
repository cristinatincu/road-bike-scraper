import com.google.common.collect.Iterables;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.pmw.tinylog.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Web scraper for SigmaSports website.
 */
public class SigmaSportsScraper extends WebScraper {

    /**
     * @param scrapeDelay   seconds for delay between scraping
     * @param bikesDao      object for database interaction
     */
    public SigmaSportsScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);
    }

    /**
     * Executes first when thread is started and starts scraping.
     * If stop thread has been triggered then scraping stops after finishing.
     * If an exception occurs during scraping then other threads receive the stop state.
     */
    public void run(){
        Logger.info("Cycle Solutions scraper starting");
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
        driver.get("https://www.sigmasports.com/bikes/road-bikes?sort=popularity&p=1");

        // number of pages is not known initially
        // so it loads next page until there are no more items to load
        while(true) {
            String pageURL = driver.getCurrentUrl();
            if (!pageURL.contains("sort=popularity"))
                break;

            int gridSize = driver.findElements(By.className("js-product-link")).size();
            for (int i = 0; i < gridSize; i++) {
                WebElement item = driver.findElements(By.className("js-product-link")).get(i);
                String url = item.getAttribute("href");
                driver.get(url);

                String name = Iterables.getLast(driver.findElements(By.className("breadcrumbs__link"))).getText();

                // if the item is not a road bike then continue to next item
                if (!name.contains("Road") || name.contains("Electric") || name.contains("Frameset")) {
                    driver.get(pageURL);
                    continue;
                }

                String imageUrl = "";
                try {
                    imageUrl = driver.findElement(By.id("js-magic-zoom-img")).getAttribute("src");
                } catch (NoSuchElementException ex) {
                    Logger.info("Image not available for " + name);
                }
                String description = driver.findElement(By.cssSelector(".product-content__content--description p")).getText();

                RoadBike roadBike = new RoadBike(shortenName(name), imageUrl, description);
                Logger.info(roadBike);
                bikesDao.addRoadBike(roadBike);

                List<WebElement> colors = driver.findElements(By.className("swatch-colour"));
                if (colors.size() > 1)
                    for (WebElement color: colors) {
                        color.click();
                        scrapePrice(driver, color.getAttribute("data-original_colour"), roadBike, url, name);
                    }
                else if (colors.size() == 0) {
                    driver.get(pageURL);
                    continue;
                } else
                    scrapePrice(driver, colors.get(0).getAttribute("data-original_colour"), roadBike, url, name);

                driver.get(pageURL);
            }

            String nextPageRef = driver.findElement(By.cssSelector("#js-listing-load-next > a")).getAttribute("href");
            driver.get(nextPageRef);
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
        WebElement sizesDiv = driver.findElement(By.cssSelector(".product-variations__container:not(.hidden)"));
        List<WebElement> sizes = sizesDiv.findElements(By.cssSelector(".product-variations__variation:not(.product-variations__variation--unavailable)"));

        for (WebElement element: sizes) {
            jsClickExecutor(driver, element);

            String size = element.getText();
            String price = driver.findElement(By.id("js-purchase-price")).getText();

            ProductComparison product = new ProductComparison(roadBike, size, shortenColor(colorName), price, url, name);

            bikesDao.addProductComparison(product);
        }
    }
}
