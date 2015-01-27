package uk.org.mattford.scoutlink.irc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.activity.ConversationsActivity;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.ServerWindow;
import uk.org.mattford.scoutlink.model.Settings;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
	
	private final int NOTIF_ID = 1;
	private final String logTag = "ScoutLink/IRCService";
	
	private boolean foreground = false;

	public void onCreate() {
		this.server = new Server();
		
		this.settings = new Settings(this);
		this.updateNotification();
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
        Message msg = new Message("Connecting to ScoutLink...");
        sw.addMessage(msg);
        Intent intent = new Intent(Broadcast.NEW_CONVERSATION).putExtra("target", "ScoutLink");
        sendBroadcast(intent);
        onNewMessage("ScoutLink");

        IRCListener listener = new IRCListener(this);
        Configuration.Builder config = new Configuration.Builder()
            .setName(settings.getString("nickname"))
            .setLogin(settings.getString("ident", "AndroidIRC"))
            .setServer("chat.scoutlink.net", 6667)
            .setRealName(settings.getString("gecos", "ScoutLink IRC for Android!"))
            .addListener(listener);

        String[] channels = settings.getStringArray("autojoin_channels");
        if (channels.length > 1 || !channels[0].equals("")) {
            for (String channel : channels) {
                if (!channel.startsWith("#")) {
                    channel = "#" + channel;
                }
                config.addAutoJoinChannel(channel);
            }
        }


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
        updateNotification();
    }
	
	public void updateNotification() {
        Intent notifIntent = new Intent(this, ConversationsActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notifIntent, 0);

        HashMap<String, Conversation> conversations = getServer().getConversations();
        ArrayList<Conversation> conversationsWithNewMsg = new ArrayList<Conversation>();
        int newMsgTotal = 0;
        ArrayList<Conversation> conversationsWithMentions = new ArrayList<Conversation>();
        int newMentionTotal = 0;
        for (Conversation conversation : conversations.values()) {
            if (conversation.hasBuffer()) {
                conversationsWithNewMsg.add(conversation);
                for (Message msg : conversation.getBuffer()) {
                    newMsgTotal++;
                    if (getConnection() != null && msg.getText().contains(getConnection().getNick())) {
                        conversationsWithMentions.add(conversation);
                        newMentionTotal++;
                    }
                }
            }
        }
        ArrayList<String> lines = new ArrayList<String>();
        if (conversationsWithNewMsg.size() > 0) {
            if (conversationsWithNewMsg.size() == 1 && newMsgTotal <= 3) {
                Conversation conv = conversationsWithNewMsg.get(0);
                for (Message msg : conv.getBuffer()) {
                    lines.add(conv.getName() + "/" + msg.getSender() + ": " + msg.getText());
                }
            } else {
                lines.add(newMsgTotal + " new messages in " + conversationsWithNewMsg.size() + " conversations.");
            }
        }
        if (conversationsWithMentions.size() > 0) {
            if (conversationsWithMentions.size() == 1) {
                lines.add(newMentionTotal + " new mentions in " + conversationsWithMentions.get(0).getName());
            } else {
                lines.add(newMentionTotal + " new mentions in " + conversationsWithMentions.size() + " conversations.");
            }
        }
        String basicText;
        if (getConnection() != null && conversationsWithNewMsg.size() == 0 && conversationsWithMentions.size() == 0) {
            lines.clear();
            basicText = "Connected as " + getConnection().getNick();
        } else {
            basicText = newMsgTotal + " new messages, " + newMentionTotal + " new mentions.";
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("New messages on ScoutLink");
        for (String line : lines) {
            inboxStyle.addLine(line);
        }
		this.notif = new NotificationCompat.Builder(this)
				.setContentTitle("ScoutLink")
				.setContentText(basicText)
                .setStyle(inboxStyle)
				.setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(intent)
				.build();
		if (this.isForeground()) {
			NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(NOTIF_ID, notif);
		}
		
	}

    public void onConnect() {
        getServer().setStatus(Server.STATUS_CONNECTED);
        setIsForeground(true);
        updateNotification();

        if (!settings.getString("nickserv_user", "").equals("") && !settings.getString("nickserv_password", "").equals("")) {
            getConnection().sendRaw().rawLineNow("NICKSERV LOGIN " + settings.getString("nickserv_user", "") + " " + settings.getString("nickserv_password", ""));
        }

        String[] commands = settings.getStringArray("command_on_connect");
        if (commands.length > 1 || !commands[0].equals("")) {
            for (String command : commands) {
                if (command.startsWith("/")) {
                    command = command.substring(1, command.length());
                }
                getConnection().sendRaw().rawLineNow(command);
            }
        }
    }

	@Override
	public Binder onBind(Intent intent) {
		return new IRCBinder(this);
	}

}
