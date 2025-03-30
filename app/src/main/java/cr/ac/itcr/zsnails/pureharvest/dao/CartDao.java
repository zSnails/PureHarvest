package cr.ac.itcr.zsnails.pureharvest.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;

@Dao
public interface CartDao {
    @Query("SELECT * FROM cart")
    List<CartItem> getAll();

    @Query("DELETE FROM cart")
    void deleteAll();

    @Query("DELETE FROM cart WHERE id = :id")
    void deleteById(Integer id);

    @Insert
    void insertAll(CartItem... items);
}
