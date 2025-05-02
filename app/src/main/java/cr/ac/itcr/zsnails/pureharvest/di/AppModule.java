package cr.ac.itcr.zsnails.pureharvest.di;

import android.content.Context;

import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import cr.ac.itcr.zsnails.pureharvest.domain.LocalCartDatabase;
import cr.ac.itcr.zsnails.pureharvest.domain.repository.ShoppingCartRepository;
import cr.ac.itcr.zsnails.pureharvest.ui.cart.ShoppingCartViewModel;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public LocalCartDatabase provideLocalCartDatabase(@ApplicationContext Context app) {
        return Room.databaseBuilder(app, LocalCartDatabase.class, "shopping-cart").build();
    }

    @Provides
    @Singleton
    public ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(5);
    }

    @Provides
    @Singleton
    public ShoppingCartRepository provideShoppingCartRepository(final LocalCartDatabase db) {
        return new ShoppingCartRepository(db);
    }
}
