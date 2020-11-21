import com.google.common.collect.Iterables;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
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

            sleep(scrapeDelay);
        }
    }

    void scrape() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver gridDriver = new ChromeDriver(options);
        gridDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        gridDriver.get("https://www.sigmasports.com/bikes/road-bikes");
        WebElement loadButton = gridDriver.findElement(By.id("js-listing-load-next"));

        while (loadButton.isDisplayed()) {
            loadButton.click();
            sleep(3);
        }
        List<String> links = new ArrayList<String>();
        List<WebElement> grid = gridDriver.findElements(By.className("js-product-link"));

        for (WebElement pageLink: grid){
            String url = pageLink.getAttribute("href");
            if (url.contains("Road") && !url.contains("Electric"))
                links.add(url);
        }

        gridDriver.close();

        Logger.info(links.size() + " bikes found");

        for (String url: links) {
            WebDriver driver = new ChromeDriver(options);
            driver.get(url);

            sleep(2);
            String name = Iterables.getLast(driver.findElements(By.className("breadcrumbs__link"))).getText();
            String image = driver.findElement(By.id("js-magic-zoom-img")).getAttribute("src");
            String description = driver.findElement(By.cssSelector(".product-content__content--description")).getText();
            description = description.split("\n")[1];

            RoadBike roadBike = new RoadBike();
            roadBike.setImage_url(image);
            roadBike.setName(name);
            roadBike.setDescription(description);

            bikesDao.addRoadBike(roadBike);

            List<WebElement> colors = driver.findElements(By.className("swatch-colour"));
            if (colors.size() > 1)
                for (WebElement color: colors) {
                    color.click();
                    sleep(2);
                    scrapePrice(driver, color.getAttribute("data-original_colour"), roadBike, url);
                }
            else
                scrapePrice(driver, colors.get(0).getAttribute("data-original_colour"), roadBike, url);
            driver.close();
        }
    };

    /** Finds price for each available size and adds it to product_comparison table
     * @param driver - Jsoup document containing the information about sizes
     * @param roadBike - the RoadBike object to link to in database
     * @param url - url of the product page
     */
    private void scrapePrice(WebDriver driver,String colorName, RoadBike roadBike, String url) {
        WebElement sizesDiv = driver.findElement(By.cssSelector(".product-variations__container:not(.hidden)"));
        List<WebElement> sizes = sizesDiv.findElements(By.cssSelector(".product-variations__variation:not(.product-variations__variation--unavailable)"));

        for (WebElement element: sizes) {
            ProductComparison product = new ProductComparison();
            element.click();

            String size = element.getText();
            String price = driver.findElement(By.id("js-purchase-price")).getText();
            price = price.substring(1).replaceAll(",","");

            product.setPrice(Float.parseFloat(price));
            product.setSize(size);
            product.setUrl(url);
            product.setRoadBike(roadBike);
            product.setColor(colorName);

            bikesDao.addProductComparison(product);
        }
    }
}
