package cr.ac.itcr.zsnails.pureharvest.entities;

import cr.ac.itcr.zsnails.pureharvest.ui.cart.Item;

public class CartDisplayItem implements Item {

    public long id;
    public String productId;
    public String name;
    public double price;
    public String type;
    public String imageUrl;
    public int amount;

    public double discountPercentage = 0.0;
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getProductId() {
        return productId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Double getPrice() {
        return price * (1 - discountPercentage / 100.0);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Integer getAmount() {
        return amount;
    }

    @Override
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Override
    public String getImage() {
        return imageUrl;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getTotalPrice() {
        return getPrice() * amount;
    }
}