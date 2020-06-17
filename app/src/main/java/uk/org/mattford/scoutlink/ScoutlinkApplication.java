package uk.org.mattford.scoutlink;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.multidex.MultiDexApplication;

public class ScoutlinkApplication extends MultiDexApplication {
    private Handler handler;

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
