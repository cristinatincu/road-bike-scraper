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
            sleep(scrapeDelay);
        }
    }

    void scrape() throws Exception {
        Document doc = Jsoup.connect("https://www.chainreactioncycles.com/road-bikes").get();
        int pages = Integer.parseInt(doc.select(".pagination").last().text());
        Logger.debug(pages + " pages found");
        List<String> links = new ArrayList<String>();

        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        for (int i = 1; i <= pages; i++) {
            WebDriver driver = new ChromeDriver(options);
            driver.get("https://www.chainreactioncycles.com/road-bikes?page=" + i);
            sleep(2);
            List<WebElement> grid = driver.findElements(By.className("products_details_container"));
            for (WebElement item: grid){
                WebElement link = item.findElement(By.tagName("a"));
                links.add(link.getAttribute("href"));
            }

            driver.close();
        }

        Logger.debug(links.size() + " bikes found");

        for (String url: links) {
            WebDriver driver = new ChromeDriver(options);
            driver.get(url);

            sleep(2);
            String name = driver.findElement(By.tagName("h1")).getText();
            String image = driver.findElement(By.className("s7_zoomviewer_staticImage")).getAttribute("src");
            String description = driver.findElement(By.id("crcPDPComponentDescription")).getText();
            description = description.split("\n")[1];

            RoadBike roadBike = new RoadBike();
            roadBike.setImage_url(image);
            roadBike.setName(name);
            roadBike.setDescription(description);

            bikesDao.addRoadBike(roadBike);

            List<WebElement> colors = driver.findElements(By.tagName("variant-option-color"));
            if (colors.size() > 1)
                for (WebElement color: colors) {
                    color.click();
                    scrapePrice(driver, roadBike, url);
                }
            else
                scrapePrice(driver, roadBike, url);
            driver.close();
        }
    };

    /** Finds price for each available size and adds it to product_comparison table
     * @param driver - Jsoup document containing the information about sizes
     * @param roadBike - the RoadBike object to link to in database
     * @param url - url of the product page
     */
    private void scrapePrice(WebDriver driver, RoadBike roadBike, String url) {
        String colorName = driver.findElement(By.className("crcPDPVariantLabelSelected")).getText();
        List<WebElement> sizes = driver.findElements(By.className("variant-option-value"));

        for (WebElement element: sizes) {
            ProductComparison product = new ProductComparison();
            element.click();

            String size = element.getText();

            String price = driver.findElement(By.className("crcPDPPriceCurrent")).getText();
            price = price.substring(1);

            product.setPrice(Float.parseFloat(price));
            product.setSize(size);
            product.setUrl(url);
            product.setRoadBike(roadBike);
            product.setColor(colorName);

            bikesDao.addProductComparison(product);
        }
    }

}
