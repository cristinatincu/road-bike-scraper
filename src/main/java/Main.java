import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

        BikesDao bikesDao = (BikesDao) context.getBean("bikesDao");

        WiggleScraper scraper1 = new WiggleScraper(2000);

        scraper1.setBikesDao(bikesDao);
        try {
            scraper1.scrapeBikes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
