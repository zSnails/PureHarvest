package cr.ac.itcr.zsnails.pureharvest.entities;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart")
public class CartItem {
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
}
