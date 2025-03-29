package cr.ac.itcr.zsnails.pureharvest.domain;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import cr.ac.itcr.zsnails.pureharvest.dao.CartDao;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;

@Database(entities = {CartItem.class}, version = 1)
public abstract class LocalCartDatabase extends RoomDatabase {
    public abstract CartDao cartDao();
}
