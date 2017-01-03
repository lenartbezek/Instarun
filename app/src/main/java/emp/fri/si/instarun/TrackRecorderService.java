package emp.fri.si.instarun;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TrackRecorderService extends Service {
    public TrackRecorderService() { }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        TrackRecorder.initialize(getApplicationContext());
        TrackRecorder.ready();
    }

    @Override
    public void onDestroy(){
        TrackRecorder.stop();
        TrackRecorder.clearUpdateListeners();
    }
}
