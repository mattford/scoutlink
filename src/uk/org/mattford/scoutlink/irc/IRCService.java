package uk.org.mattford.scoutlink.irc;

import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Server;
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
	
	private IRCConnection irc;
	private Settings settings;
	private Server server;
	private Notification notif;
	
	private final int NOTIF_ID = 007;
	private final String logTag = "ScoutLink/IRCService";
	
	public final static int ACTION_FOREGROUND = 1;
	public final static int ACTION_BACKGROUND = 0;
	
	private boolean foreground = false;
	
	
	public void onCreate() {
		this.server = new Server();
		this.irc = new IRCConnection(this);
		
		this.settings = new Settings(this);
		this.updateNotification("Not connected.");
	}
		
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	public void setForeground(int fg) {
		if (fg == ACTION_FOREGROUND) {
			startForeground(NOTIF_ID, notif);
			this.foreground = true;
		} else if (fg == ACTION_BACKGROUND && this.isForeground()) {
			stopForeground(true);
			this.foreground = false;
		}
	}
	
	public boolean isForeground() {
		return this.foreground;
	}
	
	public IRCConnection getConnection() {
		return this.irc;
	}
	
	public void connect() {
		Log.v(logTag, "Connecting...");
		if (!irc.isConnected()) {
			irc.createDefaultConversation();
			irc.setNickname(settings.getString("nickname", "SLAndroid" + Math.floor(Math.random()*100)));
			irc.setIdent(settings.getString("ident", "ScoutLinkIRC"));
			irc.setRealName(settings.getString("realName", "ScoutLink IRC for Android"));

			new Thread(new Runnable() {
				public void run() {
					Log.v("ScoutLink", "Connecting to ScoutLink...");
					try {
						irc.connect("chat.scoutlink.net");
					} catch (NickAlreadyInUseException e) {
						Log.e("ScoutLink", e.getMessage());
						e.printStackTrace();
					} catch (IOException e) {
						Log.e("ScoutLink", e.getMessage());
						e.printStackTrace();
					} catch (IrcException e) {
						Log.e("ScoutLink", e.getMessage());
						e.printStackTrace();
					}
					
				}
			}).start();
			
		}
	}
	
	public Server getServer() {
		return this.server;
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
