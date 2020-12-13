import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *  Manager of all web scrapers.
 */
public class ScraperManager {
    private List<WebScraper> scraperList = new ArrayList<WebScraper>();

    public ScraperManager(){}

    /**
     * @param scraperList   web scrapers to be started
     */
    public void setScraperList(List<WebScraper> scraperList) {
        this.scraperList = scraperList;
    }

    /**
     * Starts scraping for each element in scraper list.
     * Each web scraper is started in a separate thread.
     * Waits for user's input 'stop' to prevent scrapers from restarting.
     */
    public void startScraping() {
        for (WebScraper scraper: scraperList) {
            scraper.start();
        }

        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.nextLine();
        //waits for user to type 'stop'
        while(!userInput.equals("stop")) {
            userInput = scanner.nextLine();
        }

        for (WebScraper scraper: scraperList) {
            scraper.stopThread();
        }
    }
}
