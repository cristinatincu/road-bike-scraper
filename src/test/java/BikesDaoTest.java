import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

public class BikesDaoTest {
    static BikesDao bikesDao = new BikesDao();
    static SessionFactory sessionFactory;

    @BeforeAll
    static void setUp() {
        // uses configuration from hibernate.cfg.xml in test resources
        sessionFactory = new Configuration().configure().buildSessionFactory();
        bikesDao.setSessionFactory(sessionFactory);
    }

    @Test
    @DisplayName("Add new bike")
    public void addRoadBike_testNewBike() {
        String bikeName = "Specialized";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<RoadBike> roadBikes = session.createQuery("from RoadBike where name='" + bikeName + "'").getResultList();
        // one road_bike record in database added
        assertEquals(roadBikes.size(), 1);

        cleanDatabase(session);
    }

    @Test
    @DisplayName("Add existing bike with same name")
    public void addRoadBike_testExistingBikeWithSameName() {
        String bikeName = "Specialized";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);
        bikesDao.addRoadBike(roadBike);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<RoadBike> roadBikes = session.createQuery("from RoadBike").getResultList();
        // only one road_bike record existing in database
        assertEquals(roadBikes.size(), 1);

        cleanDatabase(session);
    }

    @Test
    @DisplayName("Add existing bike with different words order in name")
    public void addRoadBike_testExistingBikeWithSimilarName() {
        String bikeName = "Specialized DISC 2020";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);

        String bikeSimilarName = "Specialized 2020 DISC";
        RoadBike bikeSimilarBike = createRoadBike(bikeSimilarName);
        bikesDao.addRoadBike(bikeSimilarBike);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<RoadBike> roadBikes = session.createQuery("from RoadBike").getResultList();
        // only one road_bike record existing in database
        assertEquals(roadBikes.size(), 1);

        cleanDatabase(session);
    }

    @Test
    @DisplayName("Add to existing bike one with different name")
    public void addRoadBike_testExistingBikeWithDifferentName() {
        String bikeName = "Specialized ALLEZ DISC 2020";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);

        String bikeSimilarName = "Specialized 2020 DISC";
        RoadBike bikeSimilarBike = createRoadBike(bikeSimilarName);
        bikesDao.addRoadBike(bikeSimilarBike);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<RoadBike> roadBikes = session.createQuery("from RoadBike ").getResultList();
        // two road_bike records added to database
        assertEquals(roadBikes.size(), 2);

        cleanDatabase(session);
    }

    @Test
    @DisplayName("Add new comparison")
    public void addProductComparison_testNewComparison() {
        String bikeName = "Specialized";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);

        String url = "url.html";
        ProductComparison productComparison = createProductComparison(roadBike, url);
        bikesDao.addProductComparison(productComparison);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<ProductComparison> comparisons = session.createQuery("from ProductComparison where url='" + url + "'").getResultList();
        // one comparison added to database
        assertEquals(comparisons.size(), 1);

        cleanDatabase(session);
    }

    @Test
    @DisplayName("Add existing comparison")
    public void addProductComparison_testExistingComparison() {
        String bikeName = "Specialized";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);

        String url = "url.html";
        ProductComparison productComparison = createProductComparison(roadBike, url);
        bikesDao.addProductComparison(productComparison);
        bikesDao.addProductComparison(productComparison);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        List<ProductComparison> comparisons = session.createQuery("from ProductComparison").getResultList();
        // only one comparison existing in database
        assertEquals(comparisons.size(), 1);

        cleanDatabase(session);
    }

    @Test
    @DisplayName("Add existing comparison with different url")
    public void addProductComparison_testExistingComparisonWithDifferentURL() {
        String bikeName = "Specialized";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);

        String url = "url.html";
        ProductComparison productComparison = createProductComparison(roadBike, url);
        bikesDao.addProductComparison(productComparison);

        String differentURL = "url2.html";
        ProductComparison newProductComparison = createProductComparison(roadBike, differentURL);
        bikesDao.addProductComparison(newProductComparison);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        List<ProductComparison> comparisons = session.createQuery("from ProductComparison").getResultList();
        // two comparisons existing in database
        assertEquals(comparisons.size(), 2);

        cleanDatabase(session);
    }

    @Test
    @DisplayName("Add existing comparison with different color")
    public void addProductComparison_testExistingComparisonWithDifferentColor() {
        String bikeName = "Specialized";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);

        String url = "url.html";
        ProductComparison productComparison = createProductComparison(roadBike, url);
        bikesDao.addProductComparison(productComparison);

        ProductComparison productComparisonWhite = createProductComparison(roadBike, url);
        productComparisonWhite.setColor("White");
        bikesDao.addProductComparison(productComparisonWhite);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<ProductComparison> comparisons = session.createQuery("from ProductComparison").getResultList();
        // two comparisons existing in database
        assertEquals(comparisons.size(), 2);

        cleanDatabase(session);
    }

    @Test
    @DisplayName("Add existing comparison with different price")
    public void addProductComparison_testExistingComparisonWithDifferentPrice() {
        String bikeName = "Specialized";
        RoadBike roadBike = createRoadBike(bikeName);
        bikesDao.addRoadBike(roadBike);

        String url = "url.html";
        ProductComparison productComparison = createProductComparison(roadBike, url);
        bikesDao.addProductComparison(productComparison);

        productComparison.setPrice("£3999.99");
        bikesDao.addProductComparison(productComparison);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        List<ProductComparison> comparisons = session.createQuery("from ProductComparison").getResultList();
        // one comparisons existing in database with updated price
        assertEquals(comparisons.size(), 1);
        assertEquals(comparisons.get(0).getPrice(), "£3999.99");

        cleanDatabase(session);
    }

    private RoadBike createRoadBike(String bikeName) {
        return new RoadBike(
                bikeName,
                "img.jpg",
                "roadBike");
    }

    private ProductComparison createProductComparison(RoadBike roadBike, String url) {
        return new ProductComparison(
                roadBike,
                "S",
                "Black",
                "£2999.99",
                url,
                "Specialized WOMENS");
    }

    private void cleanDatabase(Session session) {
        session.createSQLQuery("truncate table road_bike").executeUpdate();
        session.createSQLQuery("truncate table product_comparison").executeUpdate();
        session.close();
    }

}
