import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

/**
 * Web scraper for Wiggle website.
 */
public class WiggleScraper extends WebScraper {

    /**
     * @param scrapeDelay   seconds for delay between scraping
     * @param bikesDao      object for database interaction
     */
    public WiggleScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);
    }

    /**
     * Executes first when thread is started and starts scraping.
     * If stop thread has been triggered then scraping stops after finishing.
     * If an exception occurs during scraping then other threads receive the stop state.
     */
    public void run(){
        Logger.info("Wiggle scraper starting");
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
     * Accesses every bike's page to scrape description, colors, sizes and price.
     * Each found road bike is added to database
     * and separate comparison objects are created for all available colors and sizes.
     *
     * @throws Exception
     */
    public void scrape() throws Exception {

        Document gridDoc = Jsoup.connect("https://www.wiggle.co.uk/cycle/road-bikes").get();
        Elements links = gridDoc.select(".bem-product-thumb__image-link--grid");

        // Iterates through the web page of each available road bike
        for (Element link: links) {
            String url = link.attr("href");
            Document itemDoc = Jsoup.connect(url).get();

            String name = itemDoc.select("#productTitle").text();
            String imageUrl = itemDoc.select("#pdpGalleryImage").attr(("src"));
            String description = itemDoc.select(".bem-pdp__product-description").text()
                    .replace(" Read More", "");

            RoadBike roadBike = new RoadBike(shortenName(name), imageUrl, description);
            Logger.info(roadBike);
            bikesDao.addRoadBike(roadBike);

            String color = itemDoc.select(".bem-sku-selector__option-label").first().text();
            // Checks if there are multiple colors available
            if (color.contains("Select")) {
                Elements colors = itemDoc.select(".qa-colour-select .bem-sku-selector__option-group-item");
                for (Element element : colors)
                    scrapePrice(itemDoc, element.text(), roadBike, url, name);

            } else {
                String colorName;
                // Some bikes have color label and color name in separate html elements
                if (color.equals("Colour:"))
                    colorName = itemDoc.select(".bem-sku-selector__option-label").get(1).text();
                else
                    colorName = color.substring(8);
                scrapePrice(itemDoc, colorName, roadBike, url, name);
            }
        }
    }

    /**
     * Finds price for each available size and adds it to product_comparison table.
     *
     * @param doc       loaded product page
     * @param colorName color of the product
     * @param roadBike  object from road_bike table to link the comparison to
     * @param url       product page
     * @param name      original bike name
     */
    private void scrapePrice(Document doc, String colorName, RoadBike roadBike, String url, String name) {
        Elements sizes = doc.select(".js-size-selections .bem-sku-selector__option-group-item");
        for (Element element: sizes) {
            String size = element.select(".bem-sku-selector__size")
                    .text().split(" ")[0];
            size = size.replace("-", "");
            if (size.contains("Small"))
                size = size.replace("Small", "S");
            else if (size.contains("Medium"))
                size = size.replace("Medium", "M");
            else if (size.contains("Large"))
                size = size.replace("Large", "L");

            String price = element.select(".bem-sku-selector__price").text();

            ProductComparison product = new ProductComparison(roadBike, size, shortenColor(colorName), price, url, name);

            bikesDao.addProductComparison(product);
        }
    }

}
