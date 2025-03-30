package cr.ac.itcr.zsnails.pureharvest.domain;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import cr.ac.itcr.zsnails.pureharvest.dao.CartDao;
import cr.ac.itcr.zsnails.pureharvest.entities.CartItem;

@Database(entities = {CartItem.class}, version = 2, autoMigrations = {
        @AutoMigration(from = 1, to = 2)
})
public abstract class LocalCartDatabase extends RoomDatabase {
    public abstract CartDao cartDao();
}
