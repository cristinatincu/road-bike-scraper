import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScraperManager {
    private List<WebScraper> scraperList = new ArrayList<WebScraper>();

    public ScraperManager(){}

    public List<WebScraper> getScraperList() {
        return scraperList;
    }

    public void setScraperList(List<WebScraper> scraperList) {
        this.scraperList = scraperList;
    }

    public void startScraping() {
        for (WebScraper scraper: scraperList) {
            scraper.start();
        }
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.nextLine();
        while(!userInput.equals("stop")) {
            userInput = scanner.nextLine();
        }

        for (WebScraper scraper: scraperList) {
            scraper.stopThread();
        }
    }
}
