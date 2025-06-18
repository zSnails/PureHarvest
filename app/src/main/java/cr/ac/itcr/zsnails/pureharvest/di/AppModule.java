package cr.ac.itcr.zsnails.pureharvest.di;

import android.content.Context;

import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import cr.ac.itcr.zsnails.pureharvest.domain.LocalCartDatabase;
import cr.ac.itcr.zsnails.pureharvest.domain.repository.ClientOrdersRepository;
import cr.ac.itcr.zsnails.pureharvest.domain.repository.ShoppingCartRepository;
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
    public ShoppingCartRepository provideShoppingCartRepository(final LocalCartDatabase db, final FirebaseFirestore firebase) {
        return new ShoppingCartRepository(db, firebase);
    }

    @Provides
    @Singleton
    public FirebaseFirestore provideFirebaseFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public ClientOrdersRepository provideClientOrdersRepository(final FirebaseFirestore db) {
        return new ClientOrdersRepository(db);
    }
}
