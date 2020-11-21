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

        Object result = session.createQuery("from RoadBike r where r.name = :name")
                .setParameter("name", roadBike.getName() ).uniqueResult();

        if (result == null) {
            session.save(roadBike);
            session.getTransaction().commit();
            Logger.info("Road bike added to database with ID: " + roadBike.getId());
        } else {
            RoadBike existingRoadBike = (RoadBike) result;
            roadBike.setId(existingRoadBike.getId());
            Logger.info("Bike already in database");
        }

        session.close();
    }

    public void addProductComparison(ProductComparison product) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Logger.info("Road_bike id: " + product.getRoadBike().getId());
        List result = session.createQuery("from ProductComparison r where r.roadBike = ?1 and r.size = ?2 and r.color = ?3")
                .setParameter(1, product.getRoadBike() )
                .setParameter(2, product.getSize())
                .setParameter(3, product.getColor())
                .list();

        if (result.isEmpty()) {
            session.save(product);
            session.getTransaction().commit();
            System.out.println("Product added to database with ID: " + product.getId());
        } else
            Logger.info("Comparison already in database");

        session.close();

    }


}
