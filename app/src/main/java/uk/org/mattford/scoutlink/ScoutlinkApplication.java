package uk.org.mattford.scoutlink;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

public class ScoutlinkApplication extends Application {
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
