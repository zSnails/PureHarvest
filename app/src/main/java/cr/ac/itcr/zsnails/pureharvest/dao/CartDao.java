package cr.ac.itcr.zsnails.pureharvest.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;

@Dao
public interface CartDao {
    @Query("SELECT * FROM CartItem")
    List<CartItem> getAll();

    @Insert
    void insertAll(CartItem... items);
}
