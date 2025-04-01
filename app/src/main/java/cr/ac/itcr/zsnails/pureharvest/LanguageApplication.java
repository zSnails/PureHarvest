package cr.ac.itcr.zsnails.pureharvest;

import android.app.Application;
import com.yariksoffice.lingver.Lingver;

public class LanguageApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Lingver.init(this);
    }
}
