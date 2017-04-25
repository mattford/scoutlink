package uk.org.mattford.scoutlink.activity;

import java.util.ArrayList;
import java.util.Arrays;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.irc.IRCBinder;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Query;
import uk.org.mattford.scoutlink.receiver.UserListReceiver;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.google.common.collect.ImmutableSortedSet;

import org.pircbotx.Channel;

public class UserListActivity extends ListActivity implements AdapterView.OnItemClickListener, ServiceConnection {

    private IRCBinder binder;
    private String channel;
    private ArrayList<String> prefixes;
    private int lastSelectedItem; // I don't like this, but it works.
    private UserListReceiver receiver;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userlist);
		channel = getIntent().getStringExtra("channel");
        prefixes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.prefixes)));
        getListView().setOnItemClickListener(this);
	}

    public void onResume() {
        super.onResume();
        this.receiver = new UserListReceiver(this, channel);
        registerReceiver(receiver, new IntentFilter(Broadcast.USER_LIST_CHANGED));
        Intent intent = new Intent(this, IRCService.class);
        bindService(intent, this, 0);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        unbindService(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userlist_context_menu, menu);
        if (binder.getService().getConnection().getUserChannelDao().getChannel(channel).isOp(binder.getService().getConnection().getUserBot())) {
            inflater.inflate(R.menu.userlist_context_menu_chanop, menu);
        }
        if (binder.getService().getConnection().getUserBot().isIrcop()) {
            inflater.inflate(R.menu.userlist_context_menu_ircop, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String nick = getListAdapter().getItem(lastSelectedItem).toString();
        if (prefixes.contains(String.valueOf(nick.charAt(0)))) {
            nick = nick.substring(1);
        }
        final Channel chan = binder.getService().getConnection().getUserChannelDao().getChannel(channel);
        final org.pircbotx.User user = binder.getService().getConnection().getUserChannelDao().getUser(nick);
        if (user == null) {
            closeContextMenu();
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_userlist_query:
                Query query = new Query(user.getNick());
                query.setSelected(true);
                binder.getService().getServer().addConversation(query);
                Intent cIntent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", user.getNick()).putExtra("selected", true);
                sendBroadcast(cIntent);
                finish();
                break;
            case R.id.action_userlist_notice:
                final EditText inputNotice = new EditText(this);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_notice_dialog_title)
                        .setView(inputNotice)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                binder.getService().getConnection().sendIRC().notice(user.getNick(), inputNotice.getText().toString());
                                Message msg = new Message("-> -"+user.getNick()+"-", inputNotice.getText().toString());
                                binder.getService().getServer().getConversation(channel).addMessage(msg);
                                binder.getService().onNewMessage(channel);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Nada.
                            }
                        })
                        .show();
                break;
            case R.id.action_userlist_kick:
                final EditText input = new EditText(this);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_kick_dialog_title)
                        .setView(input)
                        .setPositiveButton("Kick", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                chan.send().kick(user, input.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Nada.
                            }
                        })
                        .show();
                break;
            case R.id.action_userlist_kill:
                final EditText inputKill = new EditText(this);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_kill_dialog_title)
                        .setView(inputKill)
                        .setPositiveButton("Kill", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                binder.getService().getConnection().sendRaw().rawLineNow("KILL " + user.getNick() + " " + inputKill.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Nada.
                            }
                        })
                        .show();
                break;
            case R.id.action_userlist_op:
                chan.send().op(user);
                break;
            case R.id.action_userlist_deop:
                chan.send().deOp(user);
                break;
            case R.id.action_userlist_hop:
                chan.send().halfOp(user);
                break;
            case R.id.action_userlist_dehop:
                chan.send().deHalfOp(user);
                break;
            case R.id.action_userlist_owner:
                chan.send().owner(user);
                break;
            case R.id.action_userlist_deowner:
                chan.send().deOwner(user);
                break;
            case R.id.action_userlist_admin:
                chan.send().superOp(user);
                break;
            case R.id.action_userlist_deadmin:
                chan.send().deSuperOp(user);
                break;
            case R.id.action_userlist_voice:
                chan.send().voice(user);
                break;
            case R.id.action_userlist_devoice:
                chan.send().deVoice(user);
                break;


        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        lastSelectedItem = position;
        registerForContextMenu(view);
        openContextMenu(view);
        unregisterForContextMenu(view);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (IRCBinder) service;
        refreshUserList();
    }

    public void refreshUserList() {
        closeContextMenu();
        lastSelectedItem = -1;
        IRCService srvc = binder.getService();
        Channel chan = srvc.getConnection().getUserChannelDao().getChannel(channel);
        ArrayList<String> userList = new ArrayList<>();
        ImmutableSortedSet<org.pircbotx.User> users = chan.getUsers();
        for (org.pircbotx.User user : users) {
            if  (chan.isOwner(user)) {
                userList.add("~"+user.getNick());
            } else if (chan.isSuperOp(user)) {
                userList.add("&"+user.getNick());
            } else if (chan.isOp(user)) {
                userList.add("@"+user.getNick());
            } else if (chan.isHalfOp(user)) {
                userList.add("%"+user.getNick());
            } else if (chan.hasVoice(user)) {
                userList.add("+"+user.getNick());
            } else {
                userList.add(user.getNick());
            }
        }
        setListAdapter(new ArrayAdapter<>(this, R.layout.user_list_item, userList));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        binder = null;
    }
}
