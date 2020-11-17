import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

        BikesDao bikesDao = (BikesDao) context.getBean("bikesDao");
        bikesDao.addRoadBike();
        bikesDao.listRoadBikes();
    }
}
