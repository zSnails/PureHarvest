package cr.ac.itcr.zsnails.pureharvest.entities;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import cr.ac.itcr.zsnails.pureharvest.ui.cart.Item;

@Entity(tableName = "cart")
public class CartItem implements Item, Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "product_id")
    public String productId;

    @ColumnInfo(name = "amount")
    public Integer amount;

    public CartItem() {
    }

    public CartItem(@NonNull final String productId, final Integer amount) {
        this.productId = productId;
        this.amount = amount;
    }

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
        return "mock name";
    }

    @Override
    public Double getPrice() {
        return 16.53;
    }

    @Override
    public String getType() {
        return "mock type";
    }

    @Override
    public Integer getAmount() {
        return amount;
    }

    @Override
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
