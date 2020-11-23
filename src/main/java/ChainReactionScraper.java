import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.pmw.tinylog.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChainReactionScraper extends WebScraper{

    public ChainReactionScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);
    }

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
//TODO: exception handling
    //TODO: document comments
    void scrape() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.chainreactioncycles.com/road-bikes");
        driver.findElement(By.id("truste-consent-button")).click();

        int pages = driver.findElements(By.cssSelector("div.pagination > a")).size();
        for (int j = 0; j < pages; j++) {
            if (j > 0) {
                    WebElement pageButton = driver.findElements(By.cssSelector("div.pagination > a")).get(j);
                    pageButton.click();
            }
            String pageURL = driver.getCurrentUrl();

            int gridSize = driver.findElements(By.cssSelector("div.placeholder a")).size();
            for (int i = 0; i < gridSize; i++) {
                WebElement item = driver.findElements(By.cssSelector("div.placeholder a")).get(i);
                String url = item.getAttribute("href");
                jsClickExecutor(driver, item);

                String name = driver.findElement(By.tagName("h1")).getText().replace("[\\(\\)']+","");
                String[] nameSplits = name.split(" Road Bike");
                name = "";
                for (String el: nameSplits)
                    name += el.toUpperCase();

                String imageUrl = driver.findElement(By.className("s7_zoomviewer_staticImage")).getAttribute("src");
                String description = driver.findElement(By.id("crcPDPComponentDescription")).getText();
                description = description.split("\n")[1];

                RoadBike roadBike = new RoadBike(name, imageUrl, description);
                Logger.info(roadBike);
                bikesDao.addRoadBike(roadBike);

                List<WebElement> colors = driver.findElements(By.className("variant-option-color"));
                if (colors.size() > 1)
                    for (WebElement color: colors) {
                        color.click();
                        scrapePrice(driver, roadBike, url);
                    }
                else
                    scrapePrice(driver, roadBike, url);

                driver.get(pageURL);
            }
        }
        driver.close();

    }

    /** Finds price for each available size and adds it to product_comparison table
     * @param driver - Jsoup document containing the information about sizes
     * @param roadBike - the RoadBike object to link to in database
     * @param url - url of the product page
     */
    private void scrapePrice(WebDriver driver, RoadBike roadBike, String url) {
        String colorName = driver.findElement(By.className("crcPDPVariantLabelSelected")).getText();
        List<WebElement> sizes = driver.findElements(By.cssSelector("div.variant-option[data-variant='FramesSize']"));

        for (WebElement element: sizes) {
            if (!element.isDisplayed())
                continue;

            String size = element.getText().split(" ")[0];

            String price = driver.findElement(By.className("crcPDPPriceCurrent")).getText();
            price = price.substring(1);

            ProductComparison product = new ProductComparison(roadBike, size, colorName, Float.parseFloat(price), url);

            bikesDao.addProductComparison(product);
        }
    }

}
