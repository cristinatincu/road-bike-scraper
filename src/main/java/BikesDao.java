import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.pmw.tinylog.Logger;

import java.util.List;


public class BikesDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /** Adds a new road bike to the database */
    public void addRoadBike(RoadBike roadBike){
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        List result = session.createQuery("from RoadBike r where concat('%',r.name,'%') like concat('%',?1,'%')")
                .setParameter(1, roadBike.getName() ).list();
        if (result.isEmpty()) {
            session.save(roadBike);
            session.getTransaction().commit();
            Logger.info("Road bike added to database with ID: " + roadBike.getId());
        } else {
            RoadBike existingRoadBike = (RoadBike) result.get(0);
            roadBike.setId(existingRoadBike.getId());
            Logger.info("Bike already in database with ID: " + roadBike.getId());
        }

        session.close();
    }

    public void addProductComparison(ProductComparison product) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Logger.info("Road_bike id: " + product.getRoadBike().getId());
        List<ProductComparison> result = session.createQuery("from ProductComparison r where r.url = ?1")
                .setParameter(1, product.getUrl() )
                .list();

        for (ProductComparison item: result)
            if (item.equals(product))
                product.setId(item.getId());

        if (product.getId() != 0) {
            session.saveOrUpdate(product);
            session.getTransaction().commit();
            Logger.info("Product update with ID: " + product.getId());
        } else {
            session.save(product);
            session.getTransaction().commit();
            Logger.info("Product added to database with ID: " + product.getId());
        }

        session.close();
    }
}
