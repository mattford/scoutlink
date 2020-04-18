package uk.org.mattford.scoutlink;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class ScoutlinkApplication extends Application {

    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-60364072-1";
    private Handler handler;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER // Tracker used by all the apps from a company. eg: roll-up tracking.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(30);
            analytics.setDryRun(false);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            Tracker t;
            switch (trackerId) {
                case APP_TRACKER:
                    t = analytics.newTracker(R.xml.app_tracker);
                    break;
                case GLOBAL_TRACKER:
                    t = analytics.newTracker(PROPERTY_ID);
                    break;
                default:
                    t = analytics.newTracker(PROPERTY_ID);
                    break;
            }
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
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
