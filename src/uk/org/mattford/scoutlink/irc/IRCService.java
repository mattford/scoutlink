package uk.org.mattford.scoutlink.irc;

import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import uk.org.mattford.scoutlink.R;
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
	private final int NOTIF_ID = 007;
	
	
	public void onCreate() {
		this.irc = new IRCConnection();
		irc.registerService(this);
		Notification notif = new NotificationCompat.Builder(this)
			.setContentTitle("ScoutLink")
			.setContentText("Not connected")
			.setSmallIcon(R.drawable.ic_launcher)
			.build();
		startForeground(NOTIF_ID, notif);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		

		
		
		return START_STICKY;
	}
	
	public boolean connect(String nick, String ident, String realName) {
		if (!irc.isConnected()) {
			irc.setNickname(nick);
			irc.setIdent(ident);
			irc.setRealName(realName);
			
			new Thread(new Runnable() {
				public void run() {
					Log.v("ScoutLink", "Connecting to ScoutLink...");
					try {
						irc.connect("chat.scoutlink.net");
					} catch (NickAlreadyInUseException e) {
						// TODO Auto-generated catch block
						Log.e("ScoutLink", e.getMessage());
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("ScoutLink", e.getMessage());
						e.printStackTrace();
					} catch (IrcException e) {
						// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		return new IRCBinder(this);
	}

}
