package uk.org.mattford.scoutlink.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.activity.ConversationsActivity;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.ServerWindow;
import uk.org.mattford.scoutlink.model.Settings;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

public class IRCService extends Service {
	
	private PircBotX irc;
	private Settings settings;
	private Server server;
	
	private final int NOTIFICATION_ID = 1;

    public static final String ACTION_ADD_NOTIFY = "uk.org.mattford.scoutlink.IRCService.ADD_NOTIFY";
    public static final String ACTION_REMOVE_NOTIFY = "uk.org.mattford.scoutlink.IRCService.REMOVE_NOTIFY";
    public static final String ACTION_LIST_CHANNELS = "uk.org.mattford.scoutlink.IRCService.LIST_CHANNELS";

	private boolean foreground = false;

	public void onCreate() {
		this.server = new Server();
		
		this.settings = new Settings(this);
		this.updateNotification();
	}
		
	public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch(intent.getAction()) {
                case ACTION_ADD_NOTIFY:
                    if (getConnection() != null && getConnection().isConnected()) {
                        for (String item : intent.getStringArrayListExtra("items")) {

                            getConnection().sendRaw().rawLineNow("WATCH " + getConnection().getNick() + " +" + item);
                        }
                    }
                    break;
                case ACTION_REMOVE_NOTIFY:
                    if (getConnection() != null && getConnection().isConnected()) {
                        for (String item : intent.getStringArrayListExtra("items")) {
                            getConnection().sendRaw().rawLineNow("WATCH " + getConnection().getNick() + " -" + item);
                        }
                    }
                    break;
                case ACTION_LIST_CHANNELS:
                    if (getConnection() != null && getConnection().isConnected()) {
                        getConnection().sendIRC().listChannels();
                    }
            }
        }
		return START_STICKY;
	}
	
	public void setIsForeground(boolean fg) {
        if (!foreground && fg) {
            startForeground(NOTIFICATION_ID, getNotification());
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
        ServerWindow sw = new ServerWindow(getString(R.string.server_window_title));
        server.addConversation(sw);
        Message msg = new Message(getString(R.string.connect_message));
        sw.addMessage(msg);
        Intent intent = new Intent(Broadcast.NEW_CONVERSATION).putExtra("target", getString(R.string.server_window_title));
        sendBroadcast(intent);
        onNewMessage(getString(R.string.server_window_title));

        List<Configuration.ServerEntry> servers = new ArrayList<>();
        servers.add(new Configuration.ServerEntry(getString(R.string.server_address), 6667));

        IRCListener listener = new IRCListener(this);
        Configuration.Builder config = new Configuration.Builder()
            .setName(settings.getString("nickname"))
            .setLogin(settings.getString("ident", getString(R.string.default_ident)))
            .setServers(servers)
            .setRealName(settings.getString("gecos", getString(R.string.default_gecos)))
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
        final Context context = this;
        new Thread(new Runnable() {
            public void run() {
                try {
                    irc.startBot();
                } catch (Exception e) {
                    Toast.makeText(context, getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
                    onDisconnect();
                }
            }
        }).start();
	}
	
	public Server getServer() {
		return this.server;
	}

    public void onDisconnect() {
        updateNotification();
        setIsForeground(false);
        server.setStatus(Server.STATUS_DISCONNECTED);
        Intent intent = new Intent().setAction(Broadcast.DISCONNECTED);
        sendBroadcast(intent);
    }

    public void onNewMessage(String conversation) {
        Intent intent = new Intent().setAction(Broadcast.NEW_MESSAGE).putExtra("target", conversation);
        sendBroadcast(intent);
        updateNotification();
    }

    public void updateNotification() {
        if (this.isForeground()) {
            NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            Notification notification = getNotification();
            nm.notify(NOTIFICATION_ID, notification);
        }
    }
	
	public Notification getNotification() {
        Intent notifIntent = new Intent(this, ConversationsActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notifIntent, 0);

        HashMap<String, Conversation> conversations = getServer().getConversations();
        ArrayList<Conversation> conversationsWithNewMsg = new ArrayList<>();
        int newMsgTotal = 0;
        ArrayList<Conversation> conversationsWithMentions = new ArrayList<>();
        int newMentionTotal = 0;
        for (Conversation conversation : conversations.values()) {
            if (conversation.hasBuffer()) {
                conversationsWithNewMsg.add(conversation);
                ArrayList<Message> msgs = new ArrayList<>(conversation.getBuffer());
                for (Message msg : msgs) {
                    newMsgTotal++;
                    if (getConnection() != null && msg.getText().contains(getConnection().getNick())) {
                        if (!conversationsWithMentions.contains(conversation)) {
                            conversationsWithMentions.add(conversation);
                        }
                        newMentionTotal++;
                    }
                }
            }
        }
        ArrayList<String> lines = new ArrayList<>();
        if (conversationsWithNewMsg.size() > 0) {
            if (conversationsWithNewMsg.size() == 1 && newMsgTotal <= 3) {
                Conversation conv = conversationsWithNewMsg.get(0);
                ArrayList<Message> msgs = new ArrayList<>(conv.getBuffer()); // Make a copy of the buffer to avoid ConcurrentModificationException
                for (Message msg : msgs) {
                    lines.add(getString(R.string.notification_new_messages_multi, conv.getName(), msg.getSender(), msg.getText()));
                }
            } else {
                lines.add(getString(R.string.notification_new_messages, newMsgTotal, conversationsWithNewMsg.size()));
            }
        }
        if (conversationsWithMentions.size() > 0) {
            if (conversationsWithMentions.size() == 1) {
                lines.add(getString(R.string.notification_new_mentions, newMentionTotal, conversationsWithMentions.get(0).getName()));
            } else {
                lines.add(getString(R.string.notification_new_mentions_multi, newMentionTotal, conversationsWithMentions.size()));
            }
        }
        String basicText;
        if (getConnection() != null && conversationsWithNewMsg.size() == 0 && conversationsWithMentions.size() == 0) {
            lines.clear();
            basicText = getString(R.string.notification_connected, getConnection().getNick());
        } else {
            basicText = getString(R.string.notification_new_multi, newMsgTotal, newMentionTotal);
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_new_messages_inbox_title));
        for (String line : lines) {
            inboxStyle.addLine(line);
        }
		Notification notification = new NotificationCompat.Builder(this)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(basicText)
                .setStyle(inboxStyle)
				.setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentIntent(intent)
				.build();
        return notification;
		
	}

    public void onConnect() {
        getServer().setStatus(Server.STATUS_CONNECTED);
        setIsForeground(true);
        updateNotification();

        if (!settings.getString("nickserv_user", "").equals("") && !settings.getString("nickserv_password", "").equals("")) {
            irc.send().message("NickServ", "LOGIN "+settings.getString("nickserv_user", "")+" "+settings.getString("nickserv_password", ""));
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

        String[] notify_users = settings.getStringArray("notify_list");
        if (notify_users.length > 1 || !notify_users[0].equals("")) {
            for(String user : notify_users) {
                getConnection().sendRaw().rawLineNow("WATCH "+getConnection().getNick()+" +"+user);
            }
        }

        Intent intent = new Intent(Broadcast.CONNECTED);
        sendBroadcast(intent);

    }

    // Deprecated, use sendToast()
    public void onNickAlreadyInUse() {
        sendToast(getString(R.string.nick_already_in_use));
    }

    public void sendToast(final String text) {
        Handler mainThread = new Handler(getMainLooper());
        final Context context = this;
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

	@Override
	public Binder onBind(Intent intent) {
		return new IRCBinder(this);
	}

}
