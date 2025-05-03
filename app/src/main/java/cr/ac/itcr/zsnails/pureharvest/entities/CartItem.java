package cr.ac.itcr.zsnails.pureharvest.entities;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.Item;

@Entity(tableName = "cart")
public class CartItem implements Item, Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "product_id")
    public String productId;

    @ColumnInfo(name = "amount")
    public Integer amount;

    @Ignore
    public Product product;
    public void setProduct(Product product) {
        this.product = product;
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
        if (product == null) return "null product";
        return product.getName();
    }

    @Override
    public Double getPrice() {
        if (product == null) return -1.0d;
        return product.getPrice();
    }

    @Override
    public String getType() {
        return "TODO: add product type";
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
