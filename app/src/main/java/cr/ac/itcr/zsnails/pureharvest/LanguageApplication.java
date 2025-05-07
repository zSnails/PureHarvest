package cr.ac.itcr.zsnails.pureharvest;

import android.app.Application;
import com.yariksoffice.lingver.Lingver;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class LanguageApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Lingver.init(this);
    }
}
