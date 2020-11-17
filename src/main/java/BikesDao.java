import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;


public class BikesDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /** Adds a new road bike to the database */
    public void addRoadBike(){
        //Get a new Session instance from the session factory
        Session session = sessionFactory.getCurrentSession();

        //Create an instance of a University class
        RoadBike roadBike = new RoadBike();

        //Set values of University class that we want to add
        roadBike.setName("Vitus Zenium CRI Road Bike (Ultegra Di2 - 2020)");
        roadBike.setImage_url("https://www.wigglestatic.com/product-media/102420547/prod186933_Carbon-Blue%20Chameleon_NE_01.jpg?w=2000&h=2000&a=7");
        roadBike.setDescription("With a top-level carbon frameset at its core and equipped with a World Class Shimano Ultegra 2x11 Di2 electronic drivetrain and hydraulic disc brakes, this Vitus Zenium CRi road bike combines the best of pace, control, comfort and style.");

        //Start transaction
        session.beginTransaction();

        //Add university to database - will not be stored until we commit the transaction
        session.save(roadBike);

        //Commit transaction to save it to database
        session.getTransaction().commit();

        //Close the session and release database connection
        session.close();
        System.out.println("University added to database with ID: " + roadBike.getId());
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
