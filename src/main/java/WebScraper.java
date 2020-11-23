import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class WebScraper extends Thread{
    BikesDao bikesDao;
    int scrapeDelay;
    boolean stop;

    public WebScraper(int scrapeDelay, BikesDao bikesDao) {
        this.scrapeDelay = scrapeDelay;
        this.bikesDao = bikesDao;
    }

    public void stopThread() {
        stop = true;
    }

    void sleep(int n) {
        try { Thread.sleep(n * 1000); }
        catch (InterruptedException ex) { ex.printStackTrace(); stop = true; }
    }

    void jsClickExecutor(WebDriver driver, WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        }
    }


}
