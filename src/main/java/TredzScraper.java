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
            sleep(scrapeDelay);
        }
    }

    void scrape() throws Exception {
        Document doc = Jsoup.connect("https://www.tredz.co.uk/road-bikes").get();
        int pages = Integer.parseInt(doc.select(".page-number").last().text());
        Logger.debug(pages + " pages found");
        List<String> links = new ArrayList<String>();

        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        for (int i = 1; i <= pages; i++) {
            WebDriver driver = new ChromeDriver(options);
            driver.get("https://www.tredz.co.uk/road-bikes/pgn/" + i);
            sleep(2);
            WebElement grid = driver.findElement(By.className("search-results"));
            List<WebElement> pageLinks = grid.findElements(By.className("ga"));
            for (WebElement pageLink: pageLinks)
                if (pageLink.getAttribute("href") != null)
                    links.add(pageLink.getAttribute("href"));
            driver.close();
        }

        Logger.debug(links.size() + " bikes found");

        for (String url: links) {
            WebDriver driver = new ChromeDriver(options);
            driver.get(url);

            sleep(2);
            String name = "";
            try {
                name = driver.findElement(By.className("js-product-title")).getText();
            } catch (NoSuchElementException ex) {
                Logger.info("Bike unavailable: url");
                driver.close();
                continue;
            }
            String image = driver.findElement(By.id("js-product-image-zoom")).getAttribute("src");
            String description = driver.findElement(By.className("description-specification-container")).getText();
            description = description.split("\n")[1];

            RoadBike roadBike = new RoadBike();
            roadBike.setImage_url(image);
            roadBike.setName(name);
            roadBike.setDescription(description);

            bikesDao.addRoadBike(roadBike);

            List<WebElement> colors = driver.findElements(By.className("color"));
            if (colors.size() > 1)
                for (WebElement color: colors) {
                    color.click();
                    scrapePrice(driver, color.getText(), roadBike, url);
                }
            else
                scrapePrice(driver, colors.get(0).getText(), roadBike, url);
            driver.close();
        }
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
            ProductComparison product = new ProductComparison();
            element.click();

            String size = element.getText();

            String price = driver.findElement(By.className("value")).getText();
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
