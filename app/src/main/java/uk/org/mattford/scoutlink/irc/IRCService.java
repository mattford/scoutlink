package uk.org.mattford.scoutlink.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

import androidx.annotation.Nullable;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.activity.ConversationsActivity;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.ServerWindow;
import uk.org.mattford.scoutlink.model.Settings;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class IRCService extends Service {
	private Settings settings;
	private Server server;

	private final int NOTIFICATION_ID = 1;
	private final String NOTIFICATION_CHANNEL_ID = "uk.org.mattford.scoutlink.IRCService.NOTIFICATION_CHANNEL";

    public static final String ACTION_ADD_NOTIFY = "uk.org.mattford.scoutlink.IRCService.ADD_NOTIFY";
    public static final String ACTION_REMOVE_NOTIFY = "uk.org.mattford.scoutlink.IRCService.REMOVE_NOTIFY";
    public static final String ACTION_LIST_CHANNELS = "uk.org.mattford.scoutlink.IRCService.LIST_CHANNELS";

	private boolean foreground = false;

	private ArrayList<Intent> queuedIntents = new ArrayList<>();

	public void onCreate() {
		this.server = Server.getInstance();
		this.settings = new Settings(this);
		this.updateNotification();
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
    }
		
	public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (Broadcast.CONNECT.equals(intent.getAction())) {
                if (!isConnected()) {
                    connect();
                }
            } else if (isConnected()) {
                processIntent(intent);
            } else {
                queuedIntents.add(intent);
            }
        }
		return START_STICKY;
	}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_ADD_NOTIFY:
                    for (String item : intent.getStringArrayListExtra("items")) {
                        getBackgroundHandler().post(() -> getConnection().sendRaw().rawLineNow("WATCH " + getConnection().getNick() + " +" + item));
                    }
                    break;
                case ACTION_REMOVE_NOTIFY:
                    for (String item : intent.getStringArrayListExtra("items")) {
                        getBackgroundHandler().post(() -> getConnection().sendRaw().rawLineNow("WATCH " + getConnection().getNick() + " -" + item));
                    }
                    break;
                case ACTION_LIST_CHANNELS:
                    getBackgroundHandler().post(() -> getConnection().sendIRC().listChannels());
            }
        }
    }
	
	public void setIsForeground(boolean fg) {
        if (!foreground && fg) {
            createNotificationChannel();
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
		return this.server.getConnection();
	}
	
	public void connect() {
        ServerWindow sw = new ServerWindow(getString(R.string.server_window_title));
        server.addConversation(sw);
        Message msg = new Message(getString(R.string.connect_message), Message.SENDER_TYPE_SERVER, Message.TYPE_EVENT);
        sw.addMessage(msg);

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

        PircBotX irc = new PircBotX(config.buildConfiguration());
        server.setConnection(irc);
        final Context context = this;
        new Thread(() -> {
            try {
                irc.startBot();
            } catch (Exception e) {
                sendToast(context.getString(R.string.connect_failed));
                onDisconnect();
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
//        for (Conversation conversation : conversations.values()) {
//            if (conversation.hasBuffer()) {
//                conversationsWithNewMsg.add(conversation);
//                ArrayList<Message> msgs = new ArrayList<>(conversation.getBuffer());
//                for (Message msg : msgs) {
//                    newMsgTotal++;
//                    if (getConnection() != null && msg.getText().contains(getConnection().getNick())) {
//                        if (!conversationsWithMentions.contains(conversation)) {
//                            conversationsWithMentions.add(conversation);
//                        }
//                        newMentionTotal++;
//                    }
//                }
//            }
//        }
        String basicText;
        if (getConnection() != null && conversationsWithNewMsg.size() == 0 && conversationsWithMentions.size() == 0) {
            basicText = getString(R.string.notification_connected, getConnection().getNick());
        } else {
            basicText = getString(R.string.notification_new_multi, newMsgTotal, newMentionTotal);
        }

		return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(basicText)
				.setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentIntent(intent)
				.build();
	}

    public void onConnect() {
        Intent intent = new Intent(Broadcast.CONNECTED);
        sendBroadcast(intent);

        getServer().setStatus(Server.STATUS_CONNECTED);
        setIsForeground(true);
        updateNotification();

        if (!settings.getString("nickserv_user", "").equals("") && !settings.getString("nickserv_password", "").equals("")) {
            getBackgroundHandler().post(() -> getConnection().sendIRC().message("NickServ", "LOGIN "+settings.getString("nickserv_user", "")+" "+settings.getString("nickserv_password", "")));
        }

        String[] commands = settings.getStringArray("command_on_connect");
        if (commands.length > 1 || !commands[0].equals("")) {
            getBackgroundHandler().post(() -> {
                for (String command : commands) {
                    if (command.startsWith("/")) {
                        command = command.substring(1);
                    }
                    getConnection().sendRaw().rawLineNow(command);
                }
            });
        }

        String[] notify_users = settings.getStringArray("notify_list");
        if (notify_users.length > 1 || !notify_users[0].equals("")) {
            getBackgroundHandler().post(() -> {
                for(String user : notify_users) {
                    getConnection().sendRaw().rawLineNow("WATCH "+getConnection().getNick()+" +"+user);
                }
            });
        }

        for (Intent queuedIntent : queuedIntents) {
            processIntent(queuedIntent);
        }
        queuedIntents.clear();
    }

    public void sendToast(final String text) {
        Handler mainThread = new Handler(getMainLooper());
        mainThread.post(() -> Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show());
    }

    private Handler getBackgroundHandler() {
	    return ((ScoutlinkApplication)getApplication()).getBackgroundHandler();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "sl_service";
            String description = "ScoutLink Notification Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Using this method to check state as using isConnected is synchronised and
     * will ANR if long network operation is in progress. In practice the only
     * method which will take that long is connect() I think.
     *
     * @return boolean
     */
    public boolean isConnected() {
	    return server.getStatus() == Server.STATUS_CONNECTED;
    }

    public void loadLoggedMessages(Conversation conversation) {
        LogDatabase db = LogDatabase.getInstance(getApplicationContext());
        Settings settings = new Settings(this);
        boolean shouldLoadMessages = settings.getBoolean("logging_enabled") && settings.getBoolean("load_previous_messages_on_join", true);
        int messagesToLoad = settings.getInteger("previous_messages_to_load", 10);
        if (shouldLoadMessages && messagesToLoad > 0) {
            List<LogMessage> logMessages = db.logMessageDao().findConversationMessagesWithLimit(conversation.getName(), messagesToLoad);
            for (LogMessage logMessage : logMessages) {
                conversation.addMessage(logMessage.toMessage());
            }
        }
    }
}
