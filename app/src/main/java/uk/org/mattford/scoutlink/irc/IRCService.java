package uk.org.mattford.scoutlink.irc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.managers.SequentialListenerManager;

import androidx.annotation.Nullable;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.activity.ConversationsActivity;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.event.NotifyEvent;
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

    public static final String ACTION_SET_NOTIFY_LIST = "uk.org.mattford.scoutlink.IRCService.SET_NOTIFY_LIST";
    public static final String ACTION_LIST_CHANNELS = "uk.org.mattford.scoutlink.IRCService.LIST_CHANNELS";

	private boolean foreground = false;

	private final ArrayList<Intent> queuedIntents = new ArrayList<>();

	private final ArrayList<String> watchedUsers = new ArrayList<>();

	public void onCreate() {
		this.server = Server.getInstance();
		this.settings = new Settings(this);
		this.updateNotification();
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

    public void onNotify(NotifyEvent event) {
	    Settings settings = new Settings(this);
	    ArrayList<String> notifyUsers = settings.getStringArrayList("notify_list");
	    if (!notifyUsers.contains(event.getNick())) {
	        notifyUsers.add(event.getNick());
	        watchedUsers.add(event.getNick());
        }
    }

    private void processIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_SET_NOTIFY_LIST:
                    ArrayList<String> newNotifyList = intent.getStringArrayListExtra("items");
                    for (String nickname : watchedUsers) {
                        if (!newNotifyList.contains(nickname)) {
                            watchedUsers.remove(nickname);
                            getBackgroundHandler().post(() -> getConnection().sendRaw().rawLineNow("WATCH " + getConnection().getNick() + " -" + nickname));
                        }
                    }
                    for (String nickname : newNotifyList) {
                        if (!watchedUsers.contains(nickname)) {
                            watchedUsers.add(nickname);
                            getBackgroundHandler().post(() -> getConnection().sendRaw().rawLineNow("WATCH " + getConnection().getNick() + " +" + nickname));
                        }
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

	public boolean isUserBlocked(String nickname) {
	    ArrayList<String> blockedUsers = settings.getBlockedUsers();
	    return blockedUsers.contains(nickname);
    }
	
	public PircBotX getConnection() {
		return this.server.getConnection();
	}
	
	public void connect() {
        setIsForeground(true);
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
            .setRealName(settings.getString("gecos", getString(R.string.default_gecos)));

        // If we have a version of Android prior to O, the ThreadedListenerManager will
        // crash, as it uses Java8 APIs which Android < O doesn't support currently.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            config.setListenerManager(SequentialListenerManager.newDefault());
        }

        config.addListener(listener);

        ArrayList<String> channels = settings.getStringArrayList("autojoin_channels");
        for (String channel : channels) {
            config.addAutoJoinChannel(channel);
        }

        PircBotX irc = new PircBotX(config.buildConfiguration());
        server.setConnection(irc);
        server.setStatus(Server.STATUS_CONNECTING);
        updateNotification();
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
        server.setStatus(Server.STATUS_DISCONNECTED);
        updateNotification();
        setIsForeground(false);
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
        Intent notificationIntent = new Intent(this, ConversationsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        String basicText;
        switch (server.getStatus()) {
            case Server.STATUS_CONNECTED:
                basicText = getString(R.string.notification_connected, getConnection().getNick());
                break;
            case Server.STATUS_CONNECTING:
                basicText = getString(R.string.connect_message);
                break;
            case Server.STATUS_DISCONNECTED:
            default:
                basicText = getString(R.string.not_connected);
                break;
        }

		return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(basicText)
				.setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(intent)
				.build();
	}

    public void onConnect() {
        Intent intent = new Intent(Broadcast.CONNECTED);
        sendBroadcast(intent);
        getServer().setStatus(Server.STATUS_CONNECTED);
        updateNotification();

        if (!settings.getString("nickserv_user", "").equals("") && !settings.getString("nickserv_password", "").equals("")) {
            getBackgroundHandler().post(() -> getConnection().sendIRC().message("NickServ", "LOGIN "+settings.getString("nickserv_user", "")+" "+settings.getString("nickserv_password", "")));
        }

        ArrayList<String> commands = settings.getStringArrayList("command_on_connect");
        getBackgroundHandler().post(() -> {
            for (String command : commands) {
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                getConnection().sendRaw().rawLineNow(command);
            }
        });

        ArrayList<String> notifyUsers = settings.getStringArrayList("notify_list");
        getBackgroundHandler().post(() -> {
            for(String user : notifyUsers) {
                watchedUsers.add(user);
                getConnection().sendRaw().rawLineNow("WATCH "+getConnection().getNick()+" +"+user);
            }
        });

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
            CharSequence name = "General";
            String description = "Connection status and new messages";
            int importance = NotificationManager.IMPORTANCE_LOW;
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
            Collections.reverse(logMessages);
            for (LogMessage logMessage : logMessages) {
                conversation.addMessage(logMessage.toMessage(), false);
            }
        }
    }
}
