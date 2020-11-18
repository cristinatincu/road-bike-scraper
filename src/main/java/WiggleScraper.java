import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class WiggleScraper extends WebScraper {

    public WiggleScraper(int scrapeDelay) {
        super(scrapeDelay);
    }

    public void scrapeBikes() throws Exception {

        Document gridDoc = Jsoup.connect("https://www.wiggle.co.uk/cycle/road-bikes").get();

        Elements links = gridDoc.select(".bem-product-thumb__image-link--grid");

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

            Elements sizes = itemDoc.select(".bem-sku-selector__option-group-item");

            if (!sizes.isEmpty())
                bikesDao.addRoadBike(roadBike);

            for (Element element: sizes) {
                String size = element.select(".bem-sku-selector__size").text();
                String price = itemDoc.select(".bem-sku-selector__price").text();
                price = price.substring(1).replaceAll(",","");
                ProductComparison product = new ProductComparison();

                product.setPrice(Float.parseFloat(price));
                product.setSize(size);
                product.setUrl(url);
                product.setRoadBike(roadBike);

                bikesDao.addProductComparison(product);
            }

            System.out.print("succes");
        }
    }
}
