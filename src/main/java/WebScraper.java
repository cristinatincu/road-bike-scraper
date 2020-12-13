import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 */
public class WebScraper extends Thread{
    BikesDao bikesDao;
    int scrapeDelay;
    boolean stop;

    /**
     * @param scrapeDelay   seconds for delay between scraping
     * @param bikesDao      object for database interaction
     */
    public WebScraper(int scrapeDelay, BikesDao bikesDao) {
        this.scrapeDelay = scrapeDelay;
        this.bikesDao = bikesDao;
    }

    /**
     * Changes state of stop attribute to finish scraping
     */
    public void stopThread() {
        stop = true;
    }

    /**
     * Sleeps for the given amount of seconds
     *
     * @param n seconds to sleep
     */
    void sleep(int n) {
        try { Thread.sleep(n * 1000); }
        catch (InterruptedException ex) { ex.printStackTrace(); stop = true; }
    }

    /**
     * Clicks on web element.
     * If exception intercepted then a JS executor is used to retry the action
     *
     * @param driver    page containing the element to click on
     * @param element   web element to click on
     */
    void jsClickExecutor(WebDriver driver, WebElement element) {
        try {
            element.click();
        } catch (Exception e) {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Removes describing words from road bike name.
     * Leaves only model name to be added to database.
     *
     * @param name  name of road bike
     * @return      shorten name
     */
    String shortenName(String name) {
        name = name.replace("Road Bike", "").toUpperCase();
        String[] extraWords = {
                "\\)", "\\(",
                " WOMENS", " WOMEN'S", " MEN'S", " DISC",
                " TIAGRA", " CLARIS", " SORA"," ULTEGRA", " DI2", " 105", " DURA-ACE",
                " CARBON", " 20..", " -"};
        for (String word : extraWords)
            name = name.replaceAll(word, "");

        return name;
    }

    /**
     * Reduces color name to a simpler version
     *
     * @param bikeColor original bike color name
     * @return          simple version of color name
     */
    String shortenColor(String bikeColor) {
        String[] colors = {
                "Black", "Blue",
                "Red", "Green", "Orange",
                "White", "Grey", "Anthracite", "Gold", "Silver", "RIM", "BRAKE"};
        for (String color : colors)
            if (bikeColor.contains(color))
                return color;

        return bikeColor;
    }
}
