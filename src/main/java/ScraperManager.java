import java.util.ArrayList;
import java.util.List;

public class ScraperManager {
    private List<WebScraper> scraperList = new ArrayList();

    public ScraperManager(){}

    public List<WebScraper> getScraperList() {
        return scraperList;
    }

    public void setScraperList(List<WebScraper> scraperList) {
        this.scraperList = scraperList;
    }
}
