/**
 * Class to represent a physical instance of a road bike.
 * Each road bike has name, image and description.
 * Mapped to road_bike table.
 */
public class RoadBike {
    private int id;
    private String name;
    private String image_url;
    private String description;

    public RoadBike() {}

    /**
     * @param name          road bike name
     * @param image_url     url of road bike image
     * @param description   road bike description
     */
    public RoadBike(String name, String image_url, String description) {
        this.name = name;
        this.image_url = image_url;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Uses name when object is printed.
     *
     * @return  road bike name
     */
    @Override
    public String toString() {
        return name;
    }
}
