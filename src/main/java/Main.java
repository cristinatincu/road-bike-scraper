import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {

        // set spring context
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

        ScraperManager scraperManager = (ScraperManager) context.getBean("scraperManager");

        scraperManager.startScraping();
    }
}
