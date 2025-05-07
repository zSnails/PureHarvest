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
    void deleteById(Long id);

    @Insert
    long insert(CartItem item);

    @Query("UPDATE cart SET amount = :amt WHERE id = :id AND :amt >= 0")
    void updateAmount(Long id, Integer amt);

    @Query("SELECT * FROM cart WHERE product_id = :productId LIMIT 1")
    CartItem find(String productId);
}