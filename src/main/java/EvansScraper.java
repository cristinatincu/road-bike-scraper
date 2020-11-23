import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EvansScraper extends WebScraper {

    public EvansScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);
    }


    public void run(){
        Logger.info("Evans scraper starting");
        stop = false;
        while(!stop) {
            try {
                scrape();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Logger.info("Scrape finished. Waiting " + scrapeDelay + " seconds");
            sleep(scrapeDelay);
        }
    }

    void scrape() throws Exception {
        Document doc = Jsoup.connect("https://www.evanscycles.com/bikes/road-bikes").get();
        int pages = Integer.parseInt(doc.select(".MaxPageNumber").get(0).text());

        Logger.info(pages + " pages found");

        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.evanscycles.com/bikes/road-bikes");

        for (int j = 0; j < pages; j++) {
            if (j > 0) {
                String nextPageRef = driver.findElement(By.className("swipeNextClick")).getAttribute("href");
                driver.get(nextPageRef);
                sleep(2);
            }
            String pageURL = driver.getCurrentUrl();

            int gridSize = driver.findElements(By.className("ProductImageList")).size();
            for (int i = 0; i < gridSize; i++) {
                WebElement item = driver.findElements(By.className("ProductImageList")).get(i);
                String url = item.getAttribute("href");
                item.click();
                sleep(2);

                String name = "";
                try {
                    name = driver.findElement(By.className("last")).getText();
                } catch (NoSuchElementException ex) {
                    Logger.info("Bike not available:" + url);
                    driver.get(pageURL);
                    sleep(2);
                    continue;
                }

                if (!name.contains("Road") || name.contains("Electric")) {
                    driver.get(pageURL);
                    sleep(2);
                    continue;
                }

                String imageUrl = driver.findElement(By.id("imgProduct")).getAttribute("src");
                String description = driver.findElement(By.className("infoTabPage")).getText();
                description = description.split("KEY FEATURES")[0];

                RoadBike roadBike = new RoadBike(name, imageUrl, description);

                bikesDao.addRoadBike(roadBike);

                if (!driver.findElements(By.className("divColourImages")).isEmpty()) {
                    List<WebElement> colors = driver.findElements(By.className("colorImgli"));

                    for (WebElement color: colors) {
                        color.click();
                        scrapePrice(driver, roadBike, url);
                    }
                } else
                    scrapePrice(driver, roadBike, url);

                driver.get(pageURL);
                sleep(2);
            }
        }
        driver.close();

    };

    /** Finds price for each available size and adds it to product_comparison table
     * @param driver - Jsoup document containing the information about sizes
     * @param roadBike - the RoadBike object to link to in database
     * @param url - url of the product page
     */
    private void scrapePrice(WebDriver driver, RoadBike roadBike, String url) {
        String colorName = driver.findElement(By.id("colourName")).getText();
        List<WebElement> sizes = driver.findElements(By.className("sizeButtonli"));

        for (WebElement element: sizes) {
            element.click();

            String size = element.getAttribute("data-text");
            String price = driver.findElement(By.id("lblSellingPrice")).getText();
            price = price.substring(1).replaceAll(",","");

            ProductComparison product = new ProductComparison(roadBike, size, colorName, Float.parseFloat(price), url);

            bikesDao.addProductComparison(product);
        }
    }

}
