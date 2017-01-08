package emp.fri.si.instarun;

import android.app.Application;
import android.content.Context;
import net.danlew.android.joda.JodaTimeAndroid;

/**
 * App wrapper to allow access to application context from anywhere.
 */
public class InstarunApp extends Application {
    private static InstarunApp instance;

    public static InstarunApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        JodaTimeAndroid.init(this);
        super.onCreate();
    }
}
