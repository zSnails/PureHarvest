package cr.ac.itcr.zsnails.pureharvest.data.model;

import java.util.List;

public class Product {

    private String id;
    private String name;
    private double price;
    private List<String> imageUrls; // Firestore returns an array

    public Product() {
        // Required empty constructor for Firebase
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getImageUrls() { return imageUrls; }

    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }
}
