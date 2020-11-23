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

public class TredzScraper extends WebScraper {

    public TredzScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);

    }

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

    void scrape() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.tredz.co.uk/road-bikes");
        sleep(2);
        int pages = driver.findElements(By.cssSelector(".top .page-number")).size();
        Logger.debug(pages + " pages found");

        for (int j = 0; j < pages; j++) {
            if (j > 0) {
                WebElement pageButton = driver.findElements(By.cssSelector(".page-number")).get(j);
                pageButton.click();
                sleep(2);
            }
            String pageURL = driver.getCurrentUrl();

            int gridSize = driver.findElements(By.cssSelector(".search-results a.ga")).size();
            for (int i = 0; i < gridSize; i++) {
                WebElement item = driver.findElements(By.cssSelector(".search-results a.ga")).get(i);
                String url = item.getAttribute("href");
                driver.get(url);
                sleep(2);

                String name = driver.findElement(By.className("js-product-title")).getText();
                String imageUrl = driver.findElement(By.id("js-product-image-zoom")).getAttribute("src");
                String description = driver.findElement(By.className("description-specification-container")).getText();

                RoadBike roadBike = new RoadBike(name, imageUrl, description);

                bikesDao.addRoadBike(roadBike);

                List<WebElement> colors = driver.findElements(By.className("color"));
                if (colors.size() > 1)
                    for (WebElement color: colors) {
                        color.click();
                        scrapePrice(driver, color.getText(), roadBike, url);
                    }
                else
                    scrapePrice(driver, colors.get(0).getText(), roadBike, url);

                driver.get(pageURL);
            }
        }
        driver.close();
    };

    /** Finds price for each available size and adds it to product_comparison table
     * @param driver - Jsoup document containing the information about sizes
     * @param roadBike - the RoadBike object to link to in database
     * @param url - url of the product page
     */
    private void scrapePrice(WebDriver driver,String colorName, RoadBike roadBike, String url) {
        WebElement sizesDiv = driver.findElement(By.className("sku-option"));
        List<WebElement> sizes = sizesDiv.findElements(By.className("option-label"));

        for (WebElement element: sizes) {
            element.click();

            String size = element.getText();

            String price = driver.findElement(By.className("value")).getText();
            price = price.substring(1).replaceAll(",","");

            ProductComparison product = new ProductComparison(roadBike, size, colorName, Float.parseFloat(price), url);

            bikesDao.addProductComparison(product);
        }
    }

}
