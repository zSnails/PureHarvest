package cr.ac.itcr.zsnails.pureharvest.data.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private Double price;
    private List<String> imageUrls = new ArrayList<>();

    public Product() {
        // Required empty constructor for Firebase
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price != null ? price : 0.0; }

    public void setPrice(Double price) { this.price = price; }

    public List<String> getImageUrls() { return imageUrls; }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
    }

    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }
}
