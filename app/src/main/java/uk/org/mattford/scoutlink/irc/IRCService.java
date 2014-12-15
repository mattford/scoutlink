package uk.org.mattford.scoutlink.irc;

import java.io.IOException;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.ServerWindow;
import uk.org.mattford.scoutlink.model.Settings;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class IRCService extends Service {
	
	private PircBotX irc;
	private Settings settings;
	private Server server;
	private Notification notif;
	
	private final int NOTIF_ID = 007;
	private final String logTag = "ScoutLink/IRCService";
	
	private boolean foreground = false;

	public void onCreate() {
		this.server = new Server();
		
		this.settings = new Settings(this);
		this.updateNotification("Not connected.");
	}
		
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	public void setIsForeground(boolean fg) {
        if (!foreground && fg) {
            startForeground(NOTIF_ID, notif);
        } else {
            stopForeground(true);
        }
        this.foreground = fg;
	}
	
	public boolean isForeground() {
		return this.foreground;
	}
	
	public PircBotX getConnection() {
		return this.irc;
	}
	
	public void connect() {
		Log.v(logTag, "Connecting...");

        ServerWindow sw = new ServerWindow("ScoutLink");
        server.addConversation(sw);
        Intent intent = new Intent(Broadcast.NEW_CONVERSATION).putExtra("target", "ScoutLink");
        sendBroadcast(intent);

        IRCListener listener = new IRCListener(this);
        Configuration.Builder config = new Configuration.Builder()
            .setName(settings.getString("nickname"))
            .setLogin(settings.getString("login", "AndroidIRC"))
            .setServer("chat.scoutlink.net", 6667)
            .setRealName(settings.getString("realName", "ScoutLink IRC for Android!"))
            .addListener(listener);
        //config.setNickservPassword(settings.getString("password"));
        config.addAutoJoinChannel("#test");
        this.irc = new PircBotX(config.buildConfiguration());
        new Thread(new Runnable() {
            public void run() {
                try {
                    irc.startBot();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IrcException e) {
                    e.printStackTrace();
                }
            }
        }).start();


	}
	
	public Server getServer() {
		return this.server;
	}

    public void onNewMessage(String conversation) {
        Intent intent = new Intent().setAction(Broadcast.NEW_MESSAGE).putExtra("target", conversation);
        sendBroadcast(intent);
    }
	
	public void updateNotification(String text) {
		
		this.notif = new NotificationCompat.Builder(this)
				.setContentTitle("ScoutLink")
				.setContentText(text)
				.setSmallIcon(R.drawable.ic_launcher)
				.build();
		if (this.isForeground()) {
			NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(NOTIF_ID, notif);
		}
		
	}

	@Override
	public Binder onBind(Intent intent) {
		return new IRCBinder(this);
	}

}
