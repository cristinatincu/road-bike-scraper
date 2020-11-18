import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;


public class BikesDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /** Adds a new road bike to the database */
    public void addRoadBike(RoadBike roadBike){
        //Get a new Session instance from the session factory
        Session session = sessionFactory.getCurrentSession();

        //Start transaction
        session.beginTransaction();

        //Add university to database - will not be stored until we commit the transaction
        session.save(roadBike);

        //Commit transaction to save it to database
        session.getTransaction().commit();

        //Close the session and release database connection
        session.close();
        System.out.println("Road bike added to database with ID: " + roadBike.getId());
    }

    public void addProductComparison(ProductComparison product) {
        //Get a new Session instance from the session factory
        Session session = sessionFactory.getCurrentSession();

        //Start transaction
        session.beginTransaction();

        //Add university to database - will not be stored until we commit the transaction
        session.save(product);

        //Commit transaction to save it to database
        session.getTransaction().commit();

        //Close the session and release database connection
        session.close();
        System.out.println("Product added to database with ID: " + product.getId());

    }


    /** Lists all road bikes */
    public void listRoadBikes(){
        //Get a new Session instance from the session factory and start transaction
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        //Select all students and output their string representation
        List<RoadBike> roadBikesList = session.createQuery("from RoadBike").getResultList();
        for(RoadBike roadBike: roadBikesList){
            System.out.println(roadBike.toString());
        }

        //Close session and release database connection.
        session.close();
    }


}
