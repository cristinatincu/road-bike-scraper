/**
 * Comparisons of the same road bike from different online retailers.
 * Each size - color combination of the same road bike has a separate instance.
 * Mapped to product_comparison table.
 */
public class ProductComparison {
    private int id;
    private RoadBike roadBike;
    private String size;
    private String color;
    private String price;
    private String url;
    private String name;

    public ProductComparison() {
    }

    /**
     * @param roadBike  road bike to link comparison to in database
     * @param size      size of the road bike
     * @param color     color of the road bike
     * @param price     price corresponding to product's size and color
     * @param url       comparison page
     * @param name      original bike name
     */
    public ProductComparison(RoadBike roadBike, String size, String color, String price, String url, String name) {
        this.roadBike = roadBike;
        this.size = size;
        this.color = color;
        this.price = price;
        this.url = url;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
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

    /**
     * Checks if two instances of ProductComparison class are equal based on url, color and size.
     *
     * @param o instance of ProductComparison class
     * @return boolean whether objects are equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductComparison that = (ProductComparison) o;
        return size.equals(that.size) &&
                color.equals(that.color) &&
                url.equals(that.url);
    }
}
