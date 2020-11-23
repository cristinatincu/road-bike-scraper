import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

/** Scraper for Wiggle.com */
public class WiggleScraper extends WebScraper {

    public WiggleScraper(int scrapeDelay, BikesDao bikesDao) {
        super(scrapeDelay, bikesDao);
    }

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
//TODO: tinylog usage
    /** Scrapes road bikes info and adds it to database
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
            name = name.split(" Road")[0].toUpperCase();
            String imageUrl = itemDoc.select("#pdpGalleryImage").attr(("src"));
            String description = itemDoc.select(".bem-pdp__product-description--highlight").text();

            RoadBike roadBike = new RoadBike(name, imageUrl, description);
            Logger.info(roadBike);
            bikesDao.addRoadBike(roadBike);

            String color = itemDoc.select(".bem-sku-selector__option-label").first().text();
            // Checks if there are multiple colors available
            if (color.contains("Select")) {
                Elements colors = itemDoc.select(".qa-colour-select .bem-sku-selector__option-group-item");
                for (Element element : colors)
                    scrapePrice(itemDoc, element.text(), roadBike, url);

            } else {
                String colorName;
                // Some bikes have color label and color name in separate html elements
                if (color.equals("Colour:"))
                    colorName = itemDoc.select(".bem-sku-selector__option-label").get(1).text();
                else
                    colorName = color.substring(8);
                scrapePrice(itemDoc, colorName, roadBike, url);
            }
        }
    }

    /** Finds price for each available size and adds it to product_comparison table
     * @param doc - Jsoup document of the road bike page
     * @param colorName - color of the road bike
     * @param roadBike - the RoadBike object to link to in database
     * @param url - url of the product page
     */
    private void scrapePrice(Document doc, String colorName, RoadBike roadBike, String url) {
        Elements sizes = doc.select(".js-size-selections .bem-sku-selector__option-group-item");
        for (Element element: sizes) {
            String size = element.select(".bem-sku-selector__size").text();
            String price = element.select(".bem-sku-selector__price").text();
            price = price.substring(1).replaceAll(",","");

            ProductComparison product = new ProductComparison(roadBike, size, colorName, Float.parseFloat(price), url);

            bikesDao.addProductComparison(product);
        }
    }
}
