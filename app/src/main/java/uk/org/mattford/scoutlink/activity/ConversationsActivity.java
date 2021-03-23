package uk.org.mattford.scoutlink.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.command.CommandParser;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;
import uk.org.mattford.scoutlink.databinding.ActivityConversationsBinding;
import uk.org.mattford.scoutlink.fragment.ConversationListFragment;
import uk.org.mattford.scoutlink.fragment.MessageListFragment;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.Settings;
import uk.org.mattford.scoutlink.receiver.ConversationReceiver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import uk.org.mattford.scoutlink.utils.AnimationSequence;
import uk.org.mattford.scoutlink.viewmodel.ConnectionStatusViewModel;
import uk.org.mattford.scoutlink.viewmodel.ConversationListViewModel;
import uk.org.mattford.scoutlink.views.NickCompletionTextView;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.pircbotx.User;

public class ConversationsActivity extends AppCompatActivity implements ConversationListFragment.OnJoinChannelButtonClickListener {
    private ActivityConversationsBinding binding;
	private ConversationListViewModel viewModel;
    private ConversationReceiver receiver;
    private Settings settings;
    private LogDatabase db;
    private Server server;
    private boolean hasDrawerLayout;
    private Handler backgroundHandler;

	private final int JOIN_CHANNEL_RESULT = 0;

	private boolean easterEggShowing = false;

    /**
     * Required to work around NPE when screen is rotated immediately after selecting a channel, causing the reference to IRCService to be lost briefly.
     */
    private final ArrayList<String> joinChannelBuffer = new ArrayList<>();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityConversationsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(ConversationListViewModel.class);
        ConnectionStatusViewModel connectionStatusViewModel = new ViewModelProvider(this).get(ConnectionStatusViewModel.class);
        setContentView(binding.getRoot());

        settings = new Settings(this);

        hasDrawerLayout = binding.conversationsDrawerContainer != null;

        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(hasDrawerLayout);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        viewModel.getActiveConversation().observe(this, activeConversation -> {
            if (activeConversation == null) {
                return;
            }
            if ("#english".equalsIgnoreCase(activeConversation.getName())) {
                setupEasterEgg();
            }
            binding.toolbar.setTitle(activeConversation.getName());
            if (hasDrawerLayout) {
                binding.conversationsDrawerContainer.setDrawerLockMode(
                    activeConversation.getType() == Conversation.TYPE_CHANNEL ?
                        DrawerLayout.LOCK_MODE_UNLOCKED :
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                    GravityCompat.END
                );
                binding.conversationsDrawerContainer.closeDrawer(GravityCompat.START);
            }
            MessageListFragment messageListFragment = (MessageListFragment)getSupportFragmentManager().findFragmentById(R.id.conversation_view);
            if (messageListFragment != null) {
                messageListFragment.setDataSource(activeConversation);
            }
            activeConversation.getUsers().observe(this, users -> {
                // I think this can be done more fluidly with .stream().map()
                // but stuck with this unless I bump minimum API.
                ArrayList<String> userNicks = new ArrayList<>();
                for (User user : users) {
                    userNicks.add(user.getNick());
                }
                binding.input.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNicks));
            });
        });

        connectionStatusViewModel.getConnectionStatus().observe(this, connectionStatus -> binding.connectionStatus.setText(connectionStatus));
    }

	public void onResume() {
		super.onResume();

        server = Server.getInstance();
        backgroundHandler = ((ScoutlinkApplication)getApplication()).getBackgroundHandler();

        db = Room.databaseBuilder(getApplicationContext(), LogDatabase.class, "logs")
                .addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
                .build();

		this.receiver = new ConversationReceiver(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Broadcast.INVITE);
		intentFilter.addAction(Broadcast.DISCONNECTED);
		intentFilter.addAction(Broadcast.CONNECTED);
        registerReceiver(this.receiver, intentFilter);

        NickCompletionTextView newMessage = binding.input;
        newMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
                onSendButtonClick(v);
            }
            return true;
        });

        if (!settings.getBoolean("rules_viewed", false)) {
            final Context context = this;
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.view_rules_dialog_title))
                    .setMessage(getString(R.string.view_rules_dialog_message))
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, which) -> {
                        Intent intent = new Intent(context, RulesActivity.class);
                        context.startActivity(intent);
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
            settings.putBoolean("rules_viewed", true);
        }

        if (server.getConnection() != null && server.getConnection().isConnected()) {
            onConnect(false);
            return;
        }
        Intent connectIntent = new Intent(getApplicationContext(), IRCService.class);
        connectIntent.setAction(Broadcast.CONNECT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(connectIntent);
        } else {
            startService(connectIntent);
        }
	}
	
	public void onPause() {
		super.onPause();

		if (db != null) {
            db.close();
        }

		unregisterReceiver(this.receiver);
	}

    /**
     * Shows a wobbling easter egg in a (sort of) random location on the screen.
     * This can be removed after the 2021 Easter event.
     */
	private void setupEasterEgg() {
        Date now = new Date();
	    if (easterEggShowing ||
                settings.getBoolean("dont_show_easter_egg_2021", false) ||
                now.before(new Date(2021, 4, 4)) ||
                now.after(new Date(2021, 4, 4, 23, 59, 59))
        ) {
	        return;
        }
	    easterEggShowing = true;
        ImageView easterEggView = new ImageView(this);
        easterEggView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_easter_egg, getTheme()));
        easterEggView.setAlpha(0f);

        Random random = new Random();
        int marginLeft = random.nextInt(binding.getRoot().getWidth() - 50);
        int marginTop = random.nextInt(binding.getRoot().getHeight() - 50);
        RelativeLayout wrapperLayout = new RelativeLayout(this);
        wrapperLayout.addView(easterEggView);
        wrapperLayout.setPadding(marginLeft, marginTop, 0, 0);
        addContentView(
                wrapperLayout,
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        );

        easterEggView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            SpannableString message = new SpannableString("You found the #english mobile easter egg! To record this egg, upload a screenshot of this page at scoutl.ink/easter");
            Linkify.addLinks(message, Linkify.WEB_URLS);
            builder
                    .setTitle("You found an easter egg!")
                    .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_easter_egg, getTheme()))
                    .setMessage(message)
                    .setPositiveButton("Hide", (dialog, id) -> {
                        dialog.cancel();
                        wrapperLayout.setVisibility(View.GONE);
                    })
                    .setNegativeButton("Don't show this again", (dialog, id) -> {
                        dialog.cancel();
                        settings.putBoolean("dont_show_easter_egg_2021", true);
                        wrapperLayout.setVisibility(View.GONE);
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            TextView messageView = dialog.findViewById(android.R.id.message);
            messageView.setMovementMethod(LinkMovementMethod.getInstance());
        });

        int animationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);
        easterEggView.animate().alpha(1f).setDuration(animationDuration).withEndAction(() -> {
            RotateAnimation wobbleLeft = new RotateAnimation(0, -10, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
            wobbleLeft.setDuration(animationDuration / 2);
            wobbleLeft.setFillEnabled(true);
            wobbleLeft.setFillAfter(true);
            RotateAnimation wobbleRight = new RotateAnimation(-10, 10, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
            wobbleRight.setDuration(animationDuration);
            wobbleRight.setFillEnabled(true);
            wobbleRight.setFillAfter(true);
            RotateAnimation wobbleCentre = new RotateAnimation(10, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
            wobbleCentre.setDuration(animationDuration / 2);
            wobbleCentre.setFillEnabled(true);
            wobbleCentre.setFillAfter(true);
            ArrayList<Animation> animations = new ArrayList<>();
            animations.add(wobbleLeft);
            animations.add(wobbleRight);
            animations.add(wobbleCentre);
            AnimationSequence animationSequence = new AnimationSequence(animations);
            animationSequence.setRepeatEnabled(true);
            animationSequence.setRepeatDelay(animationDuration * 5);

            animationSequence.runOnView(easterEggView);
        });

    }
	
	public void onSendButtonClick(View v) {
		EditText et = binding.input;
		String message = et.getText().toString();
		Conversation conversation = viewModel.getActiveConversation().getValue();
		if (message.isEmpty() || conversation == null) {
			return;
		}
		if (message.startsWith("/")) {
			CommandParser.getInstance(this).parse(message, conversation, backgroundHandler);
		} else {
            if (conversation.getType() == (Conversation.TYPE_SERVER)) {
                Message msg = new Message(
                    getString(R.string.send_message_in_server_window),
                    Message.SENDER_TYPE_SERVER,
                    Message.TYPE_ERROR
                );
                conversation.addMessage(msg);
            } else {
                String nickname = server.getConnection().getNick();
                Message msg = new Message(nickname, message, Message.SENDER_TYPE_SELF, Message.TYPE_MESSAGE);
                conversation.addMessage(msg);

                backgroundHandler.post(() -> server.getConnection().sendIRC().message(conversation.getName(), message));
            }
		}
		et.setText("");
	}
	
	public void onInvite(final String channel) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(getString(R.string.activity_invite_title));
		adb.setMessage(getString(R.string.invited_to_channel, channel));
		adb.setPositiveButton("Yes", (dialog, which) -> backgroundHandler.post(() -> server.getConnection().sendIRC().joinChannel(channel)));
		adb.setNegativeButton("No", (dialog, which) -> {});
		adb.show();
	}

    public void onConnect(boolean initialConnection) {
	    if (initialConnection && settings.getBoolean("channel_list_on_connect", false)) {
            Intent channelListIntent = new Intent(this, ChannelListActivity.class);
            startActivityForResult(channelListIntent, JOIN_CHANNEL_RESULT);
        }

        // Join any channels we want to join...
        if (!joinChannelBuffer.isEmpty()) {
            backgroundHandler.post(() -> {
                for (String channel : joinChannelBuffer) {
                    server.getConnection().sendIRC().joinChannel(channel);
                }
                joinChannelBuffer.clear();
            });
        }
        sendBroadcast(new Intent(Broadcast.UPDATE_NOTIFICATION));
    }

	public void onDisconnect() {
        server.clearConversations();
        startActivity(new Intent(this, MainActivity.class));
        finish();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversations, menu);
        if (!hasDrawerLayout) {
            menu.findItem(R.id.action_userlist).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        ConversationListViewModel viewModel = new ViewModelProvider(this).get(ConversationListViewModel.class);
    	Conversation conversation = viewModel.getActiveConversation().getValue();
    	if (conversation == null) {
    	    return super.onOptionsItemSelected(item);
        }
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.action_close) {
            switch (conversation.getType()) {
                case Conversation.TYPE_CHANNEL:
                    backgroundHandler.post(() -> conversation.getChannel().send().part());
                    break;
                case Conversation.TYPE_QUERY:
                    server.removeConversation(conversation.getName());
                    break;
                default:
                    Toast.makeText(this, getResources().getString(R.string.close_server_window), Toast.LENGTH_SHORT).show();
                    break;
            }
        } else if (id == R.id.action_disconnect) {
            backgroundHandler.post(() -> server.getConnection().sendIRC().quitServer(settings.getString("quit_message", getString(R.string.default_quit_message))));
        } else if (id == android.R.id.home) {
            if (hasDrawerLayout && binding.conversationsDrawerContainer != null) {
                binding.conversationsDrawerContainer.openDrawer(GravityCompat.START);
            }
        } else if (id == R.id.action_userlist) {
            if (conversation.getType() == Conversation.TYPE_CHANNEL) {
                if (hasDrawerLayout && binding.conversationsDrawerContainer != null) {
                    binding.conversationsDrawerContainer.openDrawer(GravityCompat.END);
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.userlist_not_on_channel), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_join) {
            intent = new Intent(this, JoinActivity.class);
            startActivityForResult(intent, JOIN_CHANNEL_RESULT);
        } else if (id == R.id.action_channel_list) {
            onJoinChannelClick();
        } else if (id == R.id.action_channel_settings) {
            if (conversation.getType() != Conversation.TYPE_CHANNEL) {
                Toast.makeText(this, getString(R.string.channel_settings_not_channel), Toast.LENGTH_SHORT).show();
            } else if (conversation.getChannel().isOp(server.getConnection().getUserBot())) {
                intent = new Intent(this, ChannelSettingsActivity.class);
                intent.putExtra("channelName", conversation.getName());
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.channel_settings_need_op), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_rules) {
            intent = new Intent(this, RulesActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_logs) {
            intent = new Intent(this, LogListActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_aliases) {
            intent = new Intent(this, AliasesActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSettingsButtonClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JOIN_CHANNEL_RESULT && resultCode == RESULT_OK) {
            String channel = data.getStringExtra("target");
            joinChannelBuffer.add(channel);
        }
    }

    @Override
    public void onJoinChannelClick() {
        Intent intent = new Intent(this, ChannelListActivity.class);
        startActivityForResult(intent, JOIN_CHANNEL_RESULT);
    }
}
