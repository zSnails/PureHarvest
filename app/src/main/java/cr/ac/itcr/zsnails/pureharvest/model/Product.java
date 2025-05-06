package cr.ac.itcr.zsnails.pureharvest.model;

import java.util.List;

public class Product {
    public String id;
    public String name;
    public String type;
    public double rating;
    public double price;
    public String description;
    public List<String> imageUrls; // URLs Firebase Storage
    public String sellerId;

    // Optionals for coffee:
    public String certifications;
    public String flavors;
    public String acidity;
    public String body;
    public String aftertaste;
    public String ingredients;
    public String preparation;

    public Product() {
        // Empty Constructor (Firebase)
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
        this.imageUrls = imageUrls;
        this.sellerId = sellerId;
        this.certifications = certifications;
        this.flavors = flavors;
        this.acidity = acidity;
        this.body = body;
        this.aftertaste = aftertaste;
        this.ingredients = ingredients;
        this.preparation = preparation;
    }
}
