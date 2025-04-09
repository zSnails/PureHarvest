package cr.ac.itcr.zsnails.pureharvest.domain.repository;

import androidx.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import cr.ac.itcr.zsnails.pureharvest.dao.CartDao;
import cr.ac.itcr.zsnails.pureharvest.domain.LocalCartDatabase;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.Item;

public class ShoppingCartRepository {

    private final CartDao dao;

    @Inject
    public ShoppingCartRepository(
            @NonNull final LocalCartDatabase db
    ) {
        this.dao = db.cartDao();
    }

    public void deleteAll() {
        dao.deleteAll();
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }

    public void insert(CartItem... items) {
        dao.insertAll(items);
    }

    public List<CartItem> all() {
        return dao.getAll();
    }


    public void updateAmount(Item item) {
        dao.updateAmount(item.getId(), item.getAmount());
    }
}
