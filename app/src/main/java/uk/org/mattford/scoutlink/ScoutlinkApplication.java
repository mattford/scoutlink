package uk.org.mattford.scoutlink;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.multidex.MultiDexApplication;
import uk.org.mattford.scoutlink.database.LogDatabase;

public class ScoutlinkApplication extends MultiDexApplication {
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        LogDatabase.getInstance(getApplicationContext());
    }

    public Handler getBackgroundHandler()
    {
        if (this.handler != null) {
            return this.handler;
        }
        HandlerThread handlerThread = new HandlerThread("NetworkHandler");
        handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());

        return this.handler;
    }
}
