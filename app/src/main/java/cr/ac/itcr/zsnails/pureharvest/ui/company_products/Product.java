package cr.ac.itcr.zsnails.pureharvest.ui.company_products;

public class Product {
    private String name;
    private double price;
    private String firstImageUrl;

    public Product(String name, double price, String firstImageUrl) {
        this.name = name;
        this.price = price;
        this.firstImageUrl = firstImageUrl;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getFirstImageUrl() {
        return firstImageUrl;
    }
}
