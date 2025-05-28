package cr.ac.itcr.zsnails.pureharvest.data.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String type;
    private double rating;
    private double price;
    private String description;
    private List<String> imageUrls = new ArrayList<>();
    private String sellerId;
    private int totalUnitsSold;
    private Double saleDiscount;
    private List<Object> standOutPayment;

    // Optionals for coffee
    private String certifications;
    private String flavors;
    private String acidity;
    private String body;
    private String aftertaste;
    private String ingredients;
    private String preparation;

    public Product() {
        this.imageUrls = new ArrayList<>();
        this.standOutPayment = new ArrayList<>();
    }

    public Product(String name, String type, double rating, double price, String description,
                   List<String> imageUrls, String sellerId,
                   String certifications, String flavors, String acidity, String body,
                   String aftertaste, String ingredients, String preparation) {
        this.name = name;
        this.type = type;
        this.rating = rating;
        this.price = price;
        this.description = description;
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
        this.sellerId = sellerId;
        this.certifications = certifications;
        this.flavors = flavors;
        this.acidity = acidity;
        this.body = body;
        this.aftertaste = aftertaste;
        this.ingredients = ingredients;
        this.preparation = preparation;
        this.totalUnitsSold = 0;
        this.saleDiscount = 0.0;
        this.standOutPayment = new ArrayList<>();
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public double getRating() { return rating; }

    public void setRating(double rating) { this.rating = rating; }

    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<String> getImageUrls() { return imageUrls; }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
    }

    public int getTotalUnitsSold() {
        return totalUnitsSold;
    }

    public void setTotalUnitsSold(int totalUnitsSold) {
        this.totalUnitsSold = totalUnitsSold;
    }

    public Double getSaleDiscount() {
        return saleDiscount;
    }

    public void setSaleDiscount(Double saleDiscount) {
        this.saleDiscount = saleDiscount;
    }

    public String getSellerId() { return sellerId; }

    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getCertifications() { return certifications; }

    public void setCertifications(String certifications) { this.certifications = certifications; }

    public String getFlavors() { return flavors; }

    public void setFlavors(String flavors) { this.flavors = flavors; }

    public String getAcidity() { return acidity; }

    public void setAcidity(String acidity) { this.acidity = acidity; }

    public String getBody() { return body; }

    public void setBody(String body) { this.body = body; }

    public String getAftertaste() { return aftertaste; }

    public void setAftertaste(String aftertaste) { this.aftertaste = aftertaste; }

    public String getIngredients() { return ingredients; }

    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getPreparation() { return preparation; }

    public void setPreparation(String preparation) { this.preparation = preparation; }

    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    public List<Object> getStandOutPayment() { return standOutPayment; }

    public void setStandOutPayment(List<Object> standOutPayment) { this.standOutPayment = standOutPayment; }
}