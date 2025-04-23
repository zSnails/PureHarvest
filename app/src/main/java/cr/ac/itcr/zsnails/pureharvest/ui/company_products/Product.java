package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

public class Product {
    private String name;
    private String type;
    private double price;
    private double acidity;
    private String description;

    // Constructor vacío necesario para Firestore
    public Product() {}

    public Product(String name, String type, double price, double acidity, String description) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.acidity = acidity;
        this.description = description;
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAcidity() {
        return acidity;
    }

    public void setAcidity(double acidity) {
        this.acidity = acidity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

