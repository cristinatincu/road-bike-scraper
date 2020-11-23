public class ProductComparison {
    private int id;
    private RoadBike roadBike;
    private String size;
    private String color;
    private float price;
    private String url;

    public ProductComparison() {
    }

    public ProductComparison(RoadBike roadBike, String size, String color, float price, String url) {
        this.roadBike = roadBike;
        this.size = size;
        this.color = color;
        this.price = price;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RoadBike getRoadBike() {
        return roadBike;
    }

    public void setRoadBike(RoadBike roadBike) {
        this.roadBike = roadBike;
    }
}
