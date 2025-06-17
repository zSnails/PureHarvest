package cr.ac.itcr.zsnails.pureharvest.ui.orders;

public class PurchasedProductOrder {
    private String productId;
    private String name;
    private double price;
    private String imageUrl;
    private int quantity; // Added quantity

    public PurchasedProductOrder(String productId, String name, double price, String imageUrl, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantity() { // Getter for quantity
        return quantity;
    }
}