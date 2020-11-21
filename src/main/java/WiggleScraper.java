import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import java.util.List;

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

            sleep(scrapeDelay);
        }
    }

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
            String image = itemDoc.select("#pdpGalleryImage").attr(("src"));
            String description = itemDoc.select(".bem-pdp__product-description--highlight").text();

            RoadBike roadBike = new RoadBike();
            roadBike.setImage_url(image);
            roadBike.setName(name);
            roadBike.setDescription(description);

            bikesDao.addRoadBike(roadBike);

            Element color = itemDoc.select(".bem-sku-selector__option-label").get(0);
            // Checks if there are multiple colors available
            if (color.text().contains("Select")) {

                Elements colorSelector = itemDoc.select(".qa-colour-select");
                Elements colors = colorSelector.select(".bem-sku-selector__option-group-item");

                for (Element element : colors) {
                    Elements prices = itemDoc.select(".js-size-selections");
                    scrapePrice(prices, element.text(), roadBike, url);
                }

            } else {
                String color_name = "";
                // Some bikes have color label and color name in separate html elements
                if (color.text().equals("Colour:"))
                    color_name = itemDoc.select(".bem-sku-selector__option-label").get(1).text();
                else
                    color_name = color.text().substring(8);
                Elements prices = itemDoc.select(".js-size-selections");
                scrapePrice(prices, color_name, roadBike, url);
            }
        }
    }

    /** Finds price for each available size and adds it to product_comparison table
     * @param prices - Jsoup document containing the information about sizes
     * @param color_name - color of the road bike
     * @param roadBike - the RoadBike object to link to in database
     * @param url - url of the product page
     */
    private void scrapePrice(Elements prices, String color_name, RoadBike roadBike, String url) {
        Elements sizes = prices.select(".bem-sku-selector__option-group-item");
        for (Element element: sizes) {
            ProductComparison product = new ProductComparison();

            String size = element.select(".bem-sku-selector__size").text();

            String price = element.select(".bem-sku-selector__price").text();
            price = price.substring(1).replaceAll(",","");

            product.setPrice(Float.parseFloat(price));
            product.setSize(size);
            product.setUrl(url);
            product.setRoadBike(roadBike);
            product.setColor(color_name);

            bikesDao.addProductComparison(product);
        }
    }
}
