import com.google.common.collect.Iterables;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.pmw.tinylog.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SigmaSportsScraper extends WebScraper {

    public SigmaSportsScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);
    }

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

    void scrape() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.sigmasports.com/bikes/road-bikes?sort=popularity&p=1");

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

                if (!name.contains("Road") || name.contains("Electric") || name.contains("Frameset")) {
                    driver.get(pageURL);
                    continue;
                }

                String[] nameSplits = name.split(" Road Bike");
                name = "";
                for (String el: nameSplits)
                    name += el.toUpperCase();

                String imageUrl = driver.findElement(By.id("js-magic-zoom-img")).getAttribute("src");
                String description = driver.findElement(By.cssSelector(".product-content__content--description")).getText();
                description = description.split("\n")[1];

                RoadBike roadBike = new RoadBike(name, imageUrl, description);
                Logger.info(roadBike);
                bikesDao.addRoadBike(roadBike);

                List<WebElement> colors = driver.findElements(By.className("swatch-colour"));
                if (colors.size() > 1)
                    for (WebElement color: colors) {
                        color.click();
                        scrapePrice(driver, color.getAttribute("data-original_colour"), roadBike, url);
                    }
                else if (colors.size() == 0) {
                    driver.get(pageURL);
                    continue;
                } else
                    scrapePrice(driver, colors.get(0).getAttribute("data-original_colour"), roadBike, url);

                driver.get(pageURL);
            }

            String nextPageRef = driver.findElement(By.cssSelector("#js-listing-load-next > a")).getAttribute("href");
            driver.get(nextPageRef);
        }
        driver.close();
    }

    /** Finds price for each available size and adds it to product_comparison table
     * @param driver - Jsoup document containing the information about sizes
     * @param roadBike - the RoadBike object to link to in database
     * @param url - url of the product page
     */
    private void scrapePrice(WebDriver driver,String colorName, RoadBike roadBike, String url) {
        WebElement sizesDiv = driver.findElement(By.cssSelector(".product-variations__container:not(.hidden)"));
        List<WebElement> sizes = sizesDiv.findElements(By.cssSelector(".product-variations__variation:not(.product-variations__variation--unavailable)"));

        for (WebElement element: sizes) {
            element.click();

            String size = element.getText();
            String price = driver.findElement(By.id("js-purchase-price")).getText();
            price = price.substring(1).replaceAll(",","");

            ProductComparison product = new ProductComparison(roadBike, size, colorName, Float.parseFloat(price), url);

            bikesDao.addProductComparison(product);
        }
    }
}
