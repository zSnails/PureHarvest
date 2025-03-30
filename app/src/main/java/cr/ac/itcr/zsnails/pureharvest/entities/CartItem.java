package cr.ac.itcr.zsnails.pureharvest.entities;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart")
public class CartItem {
    @PrimaryKey
    public long id;

    @ColumnInfo(name = "product_id")
    public String productId;
}
