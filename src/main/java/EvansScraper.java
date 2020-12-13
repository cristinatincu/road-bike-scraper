import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
 * Web scraper for EansCycles website.
 */
public class EvansScraper extends WebScraper {

    /**
     * @param scrapeDelay   seconds to wait after finishing scraping
     * @param bikesDao      object for database interaction
     */
    public EvansScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);
    }

    /**
     * Executes first when thread is started and starts scraping.
     * If stop thread has been triggered then scraping stops after finishing.
     * If an exception occurs during scraping then other threads receive the stop state.
     */
    public void run(){
        Logger.info("Evans scraper starting");
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
        // get number of pages using Jsoup as selenium does not capture this element
        Document doc = Jsoup.connect("https://www.evanscycles.com/bikes/road-bikes").get();
        int pages = Integer.parseInt(doc.select(".MaxPageNumber").get(0).text());

        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.evanscycles.com/bikes/road-bikes");

        for (int j = 0; j < pages; j++) {
            // press on next page button after scraping first page
            if (j > 0) {
                String nextPageRef = driver.findElement(By.className("swipeNextClick")).getAttribute("href");
                driver.get(nextPageRef);
            }
            String pageURL = driver.getCurrentUrl();

            int gridSize = driver.findElements(By.className("ProductImageList")).size();
            for (int i = 0; i < gridSize; i++) {
                WebElement item = driver.findElements(By.className("ProductImageList")).get(i);
                String url = item.getAttribute("href");
                item.click();

                String name;
                try {
                    // sometimes items part of the list are not available
                    name = driver.findElement(By.className("last")).getText();
                } catch (NoSuchElementException ex) {
                    Logger.info("Bike not available:" + url);
                    driver.get(pageURL);
                    continue;
                }

                // exclude non-road bikes and electric ones
                if (!name.contains("Road") || name.contains("Electric")) {
                    driver.get(pageURL);
                    continue;
                }

                String imageUrl = driver.findElement(By.id("imgProduct")).getAttribute("src");
                // get only the first paragraph of the description
                String description = driver.findElement(By.className("infoTabPage")).getText().split("\n")[1];
                description = description.split("KEY FEATURES")[0];
                RoadBike roadBike = new RoadBike(shortenName(name), imageUrl, description);
                Logger.info(roadBike);
                bikesDao.addRoadBike(roadBike);

                // check if there are multiple colors available
                if (!driver.findElements(By.className("divColourImages")).isEmpty()) {
                    List<WebElement> colors = driver.findElements(By.className("colorImgli"));

                    for (WebElement color: colors) {
                        color.click(); // click on color to update price
                        scrapePrice(driver, roadBike, url, name);
                    }

                } else
                    scrapePrice(driver, roadBike, url, name);

                driver.get(pageURL);
            }
        }
        driver.close();
    }

    /**
     * Finds price for each available size and adds comparison to database
     *
     * @param driver    loaded product page
     * @param roadBike  road bike to link to in database
     * @param url       product page
     * @param name      original bike name
     */
    private void scrapePrice(WebDriver driver, RoadBike roadBike, String url, String name) {
        String colorName = driver.findElement(By.id("colourName")).getText();
        colorName = colorName.split(" 2")[0];
        List<WebElement> sizes = driver.findElements(By.className("sizeButtonli"));

        for (WebElement element: sizes) {
            element.click(); // click on size to update price

            String size = element.getAttribute("data-text");
            String price = driver.findElement(By.id("lblSellingPrice")).getText();

            ProductComparison product = new ProductComparison(roadBike, size, shortenColor(colorName), price, url, name);
            bikesDao.addProductComparison(product);
        }
    }
}
