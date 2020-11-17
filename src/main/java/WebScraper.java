public class WebScraper {
    BikesDao bikesDao;
    int scrapeDelay;

    public BikesDao getBikesDao() {
        return bikesDao;
    }

    public void setBikesDao(BikesDao bikesDao) {
        this.bikesDao = bikesDao;
    }

    public int getScrapeDelay() {
        return scrapeDelay;
    }

    public void setScrapeDelay(int scrapeDelay) {
        this.scrapeDelay = scrapeDelay;
    }
}
