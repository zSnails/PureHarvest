package cr.ac.itcr.zsnails.pureharvest.di;

import android.content.Context;

import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import cr.ac.itcr.zsnails.pureharvest.domain.LocalCartDatabase;
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
}
