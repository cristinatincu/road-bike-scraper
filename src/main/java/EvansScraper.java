import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

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

            sleep(scrapeDelay);
        }
    }

    void scrape() throws Exception {
        Document doc = Jsoup.connect("https://www.evanscycles.com/bikes/road-bikes").get();
        int pages = Integer.parseInt(doc.select(".MaxPageNumber").get(0).text());
        Logger.info(pages + " pages found");
        List<String> links = new ArrayList<String>();

        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        for (int i = 1; i <= pages; i++) {
            WebDriver driver = new ChromeDriver(options);
            driver.get("https://www.evanscycles.com/bikes/road-bikes#dcp=" + i + "&dppp=24&OrderBy=rank");
            sleep(2);
            List<WebElement> pageLinks = driver.findElements(By.className("ProductImageList"));
            for (WebElement pageLink: pageLinks) {
                String url = pageLink.getAttribute("href");
                if (url.contains("road") && !url.contains("electric"))
                    links.add(url);
            }
            driver.close();
        }

        Logger.info(links.size() + " bikes found");

        for (String url: links) {
            WebDriver driver = new ChromeDriver(options);
            driver.get(url);

            sleep(2);
            String name = driver.findElement(By.className("last")).getText();

            String image = driver.findElement(By.id("imgProduct")).getAttribute("src");
            String description = driver.findElement(By.className("infoTabPage")).getText();
            description = description.split("KEY FEATURES")[0];

            RoadBike roadBike = new RoadBike();
            roadBike.setImage_url(image);
            roadBike.setName(name);
            roadBike.setDescription(description);

            bikesDao.addRoadBike(roadBike);

            if (!driver.findElements(By.className("divColourImages")).isEmpty()) {
                List<WebElement> colors = driver.findElements(By.className("colorImgli"));

                for (WebElement color: colors) {
                    color.click();
                    scrapePrice(driver, roadBike, url);
                }
            } else
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
        String colorName = driver.findElement(By.id("colourName")).getText();
        List<WebElement> sizes = driver.findElements(By.className("sizeButtonli"));

        for (WebElement element: sizes) {
            ProductComparison product = new ProductComparison();
            element.click();

            String size = element.getAttribute("data-text");

            String price = driver.findElement(By.id("lblSellingPrice")).getText();
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
