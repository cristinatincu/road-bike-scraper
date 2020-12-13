import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.pmw.tinylog.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Web scraper for chainreactioncycles website.
 */
public class ChainReactionScraper extends WebScraper{

    /**
     * @param scrapeDelay   seconds to wait after finishing scraping
     * @param bikesDao      object for database interaction
     */
    public ChainReactionScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);
    }

    /**
     * Executes first when thread is started and starts scraping.
     * If stop thread has been triggered then scraping stops after finishing.
     * If an exception occurs during scraping then other threads receive the stop state.
     */
    public void run(){
        Logger.info("Chain Reaction scraper starting");
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
     * Each found road bike is added to database
     * and separate comparison objects are created for all available colors and sizes.
     *
     * @throws Exception
     */
    void scrape() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.chainreactioncycles.com/road-bikes");
        // accept cookies
        driver.findElement(By.id("truste-consent-button")).click();

        int pages = driver.findElements(By.cssSelector("div.pagination > a")).size();
        for (int j = 0; j < pages; j++) {
            // press on next page button after scraping first page
            if (j > 0) {
                WebElement pageButton = driver.findElements(By.cssSelector("div.pagination > a")).get(j);
                pageButton.click();
            }
            String pageURL = driver.getCurrentUrl();

            int gridSize = driver.findElements(By.cssSelector("div.placeholder a")).size();
            for (int i = 0; i < gridSize; i++) {
                WebElement item = driver.findElements(By.cssSelector("div.placeholder a")).get(i);
                String url = item.getAttribute("href");
                //click on road bike to access its page
                jsClickExecutor(driver, item);

                String name = driver.findElement(By.tagName("h1")).getText();
                String imageUrl = driver.findElement(By.className("s7_zoomviewer_staticImage")).getAttribute("src");
                String description = driver.findElement(By.cssSelector("#crcPDPComponentDescription p")).getText();

                RoadBike roadBike = new RoadBike(shortenName(name), imageUrl, description);
                Logger.info(roadBike);
                bikesDao.addRoadBike(roadBike);

                List<WebElement> colors = driver.findElements(By.className("variant-option-color"));
                for (WebElement color: colors) {
                    // click on color to update price
                    jsClickExecutor(driver, color);

                    String colorName = driver.findElement(By.className("crcPDPVariantLabelSelected")).getText();
                    List<WebElement> sizes = driver.findElements(By.cssSelector("div.variant-option[data-variant='FramesSize']"));

                    for (WebElement element: sizes) {
                        if (!element.isDisplayed())
                            continue; // size not available for current color if it is not displayed

                        // click on size to update price
                        jsClickExecutor(driver, element);

                        String size = element.getText().split(" ")[0]; // remove any text that is not size
                        String price = driver.findElement(By.className("crcPDPPriceCurrent")).getText();

                        ProductComparison comparison = new ProductComparison(roadBike, size, shortenColor(colorName), price, url, name);
                        bikesDao.addProductComparison(comparison);
                    }
                }
                // return to page that lists road bikes
                driver.get(pageURL);
            }
        }
        driver.close();
    }
}
