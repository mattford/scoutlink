package uk.org.mattford.scoutlink.irc;

import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.Scoutlink;
import uk.org.mattford.scoutlink.model.Message;
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
	private final int NOTIF_ID = 007;
	private final String logTag = "ScoutLink/IRCService";
	
	public final String ACTION_FOREGROUND = "uk.org.mattford.scoutlink.irc.IRCService.ACTION_FOREGROUND";
	public final String ACTION_BACKGROUND = "uk.org.mattford.scoutlink.irc.IRCService.ACTION_BACKGROUND";
	
	
	public void onCreate() {
		Log.v(logTag, "New instance of IRCService created.");
		this.irc = new IRCConnection(this);
		this.settings = new Settings(this);
		Notification notif = new NotificationCompat.Builder(this)
			.setContentTitle("ScoutLink")
			.setContentText("Not connected")
			.setSmallIcon(R.drawable.ic_launcher)
			.build();
		startForeground(NOTIF_ID, notif);
	}
	
	public void onDestroy() {
		Log.v(logTag, "Service is being destroyed!");
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	public IRCConnection getConnection() {
		return this.irc;
	}
	
	public boolean connect() {
		if (!this.irc.isConnected()) {
			this.irc.createDefaultConversation();
			this.irc.setNickname(settings.getString("nickname", "SLAndroid" + Math.floor(Math.random()*100)));
			this.irc.setIdent(settings.getString("ident", "ScoutLinkIRC"));
			this.irc.setRealName(settings.getString("realName", "ScoutLink IRC for Android"));

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
		return true;
	}
	
	public void updateNotification(String text) {
		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notif = new NotificationCompat.Builder(this)
				.setContentTitle("ScoutLink")
				.setContentText(text)
				.setSmallIcon(R.drawable.ic_launcher)
				.build();
		nm.notify(NOTIF_ID, notif);
		
	}

	@Override
	public Binder onBind(Intent intent) {
		return new IRCBinder(this);
	}

}
