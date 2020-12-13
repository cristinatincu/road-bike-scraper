import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.pmw.tinylog.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Interacts with product_comparison database.
 */
public class BikesDao {

    private SessionFactory sessionFactory;

    /**
     * @param sessionFactory session factory to be set one per application
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Adds road bike to road_bike table.
     * Checks if database contains similar names to the new object, regardless of words order.
     * If yes then the object receives the ID of the existing record.
     * otherwise a new record is added to database
     *
     * @param roadBike newly scraped road bike
     */
    public void addRoadBike(RoadBike roadBike){
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        // create list of words from name
        String[] nameWords = roadBike.getName().split(" ");
        String query = "%" + nameWords[0] + "%";

        // if name has more than one word then it is added to query
        if (nameWords.length > 1)
            query += nameWords[1] + "%";

        List<RoadBike> result = session.createQuery("from RoadBike r where r.name like ?1")
                .setParameter(1, query ).list();

        for (RoadBike similarBike : result) {
            // if names are not same
            if ( !sortString(roadBike.getName()).equals( sortString( similarBike.getName() ) ) )
                continue;

            Logger.info("Similar bike exists in database");
            roadBike.setId(similarBike.getId());
            break;
        }

        // if similar bike was not found
        if (roadBike.getId() == 0) {
            session.save(roadBike);
            session.getTransaction().commit();
            Logger.info("Road bike added to database with ID: " + roadBike.getId());
        }

        session.close();
    }

    /**
     * Adds product for comparison to product_comparison table.
     * Checks if similar comparisons exist in database based on url
     * If there is one with same color and size then checks if price needs to be updated
     * otherwise the object is saved to database.
     *
     * @param comparison newly scraped comparison
     */
    public void addProductComparison(ProductComparison comparison) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<ProductComparison> result = session.createQuery("from ProductComparison r where r.url = ?1")
                .setParameter(1, comparison.getUrl() )
                .list();

        for (ProductComparison item : result)
            // if has same color, size and url
            if (item.equals(comparison)) {
                comparison.setId(item.getId());
                if (!item.getPrice().equals(comparison.getPrice())) {
                    item.setPrice(comparison.getPrice());
                    session.update(item);
                    session.getTransaction().commit();
                    Logger.info("Product update with ID: " + item.getId());
                } else
                    Logger.info("Product already in database");
                break;
            }

        //if same comparison was not found
        if (comparison.getId() == 0) {
            session.save(comparison);
            session.getTransaction().commit();
            Logger.info("Product added to database with ID: " + comparison.getId());
        }

        session.close();
    }

    /**
     * Sorts string by creating a list of characters, excluding spaces
     * @param str   string to be sorted
     * @return      string of sorted characters excluding spaces
     */
    private String sortString(String str) {
        str = str.trim()
                .replaceAll("\\s+", "");

        char[] chars = str.toCharArray();

        Arrays.sort(chars);
        return new String(chars);
    }
}
