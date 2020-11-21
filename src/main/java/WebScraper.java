import org.pmw.tinylog.Logger;

public class WebScraper extends Thread{
    BikesDao bikesDao;
    int scrapeDelay;
    boolean stop;

    public WebScraper(int scrapeDelay, BikesDao bikesDao) {
        this.scrapeDelay = scrapeDelay;
        this.bikesDao = bikesDao;
    }

    void sleep(int n) {
        try { Thread.sleep(n * 1000); }
        catch (InterruptedException ex) { ex.printStackTrace(); stop = true; }
    }

    public void stopThread() {
        stop = true;
    }
}
