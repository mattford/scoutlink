package uk.org.mattford.scoutlink.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.pircbotx.Channel;
import org.pircbotx.User;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.ScoutlinkApplication;
import uk.org.mattford.scoutlink.adapter.UserListRecyclerViewAdapter;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Query;
import uk.org.mattford.scoutlink.model.Server;
import uk.org.mattford.scoutlink.model.Settings;
import uk.org.mattford.scoutlink.viewmodel.ConversationListViewModel;

public class UserListFragment extends Fragment implements PopupMenu.OnMenuItemClickListener, UserListRecyclerViewAdapter.OnUserListItemClickListener {
    private RecyclerView recyclerView;
    private User selectedUser;
    private ConversationListViewModel viewModel;
    private Server server;
    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server = Server.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(ConversationListViewModel.class);
        setHasOptionsMenu(true);
        settings = new Settings(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getActiveConversation().observe(getViewLifecycleOwner(), activeConversation -> {
            if (activeConversation == null) {
                return;
            }
            activeConversation.getUsers().observe(getViewLifecycleOwner(), users -> recyclerView.setAdapter(new UserListRecyclerViewAdapter(users, settings, activeConversation.getChannel(), this)));
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        return view;
    }

    private void showPopup(View view) {
        PopupMenu popup = new PopupMenu(requireActivity().getApplicationContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        Menu menu = popup.getMenu();
        menu.clear();
        inflater.inflate(R.menu.userlist_context_menu, menu);
        ArrayList<String> blockedUsers = settings.getBlockedUsers();
        if (blockedUsers.contains(selectedUser.getNick())) {
            menu.removeItem(R.id.action_userlist_block);
        } else {
            menu.removeItem(R.id.action_userlist_unblock);
        }
        Conversation activeConversation = viewModel.getActiveConversation().getValue();
        if (activeConversation != null &&
                activeConversation.getChannel() != null &&
                activeConversation.getChannel().isOp(server.getConnection().getUserBot())
        ) {
            inflater.inflate(R.menu.userlist_context_menu_chanop, menu);
        }
        if (server.getConnection().getUserBot().isIrcop()) {
            inflater.inflate(R.menu.userlist_context_menu_ircop, menu);
        }
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Conversation activeConversation = viewModel.getActiveConversation().getValue();
        Handler backgroundHandler = ((ScoutlinkApplication)requireActivity().getApplication()).getBackgroundHandler();
        if (selectedUser == null || activeConversation == null) {
            return false;
        }
        Channel channel = activeConversation.getChannel();
        String nickname = selectedUser.getNick();
        Settings settings = new Settings(getContext());
        int itemId = item.getItemId();
        if (itemId == R.id.action_userlist_query) {
            Query query = new Query(selectedUser.getNick());
            server.addConversation(query);
        } else if (itemId == R.id.action_userlist_notice) {
            final EditText inputNotice = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.action_notice_dialog_title)
                    .setView(inputNotice)
                    .setPositiveButton("Send", (dialog, whichButton) -> {
                        backgroundHandler.post(() -> server.getConnection().sendIRC().notice(nickname, inputNotice.getText().toString()));
                        Message msg = new Message(nickname, inputNotice.getText().toString(), Message.SENDER_TYPE_SELF, Message.TYPE_NOTICE);
                        activeConversation.addMessage(msg);
                    })
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {
                    })
                    .show();
        } else if (itemId == R.id.action_userlist_block) {
            settings.blockUser(nickname);
            Toast.makeText(getContext(), getString(R.string.user_blocked, nickname), Toast.LENGTH_LONG).show();
            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        } else if (itemId == R.id.action_userlist_unblock) {
            settings.unblockUser(nickname);
            Toast.makeText(getContext(), getString(R.string.user_unblocked, nickname), Toast.LENGTH_LONG).show();
            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        } else if (itemId == R.id.action_userlist_kick) {
            final EditText input = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.action_kick_dialog_title)
                    .setView(input)
                    .setPositiveButton("Kick", (dialog, whichButton) -> backgroundHandler.post(() -> channel.send().kick(selectedUser, input.getText().toString())))
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {
                    })
                    .show();
        } else if (itemId == R.id.action_userlist_kill) {
            final EditText inputKill = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.action_kill_dialog_title)
                    .setView(inputKill)
                    .setPositiveButton("Kill", (dialog, whichButton) -> backgroundHandler.post(() -> server.getConnection().sendRaw().rawLineNow("KILL " + nickname + " " + inputKill.getText().toString())))
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {
                    })
                    .show();
        } else if (itemId == R.id.action_userlist_op) {
            backgroundHandler.post(() -> channel.send().op(selectedUser));
        } else if (itemId == R.id.action_userlist_deop) {
            backgroundHandler.post(() -> channel.send().deOp(selectedUser));
        } else if (itemId == R.id.action_userlist_hop) {
            backgroundHandler.post(() -> channel.send().halfOp(selectedUser));
        } else if (itemId == R.id.action_userlist_dehop) {
            backgroundHandler.post(() -> channel.send().deHalfOp(selectedUser));
        } else if (itemId == R.id.action_userlist_owner) {
            backgroundHandler.post(() -> channel.send().owner(selectedUser));
        } else if (itemId == R.id.action_userlist_deowner) {
            backgroundHandler.post(() -> channel.send().deOwner(selectedUser));
        } else if (itemId == R.id.action_userlist_admin) {
            backgroundHandler.post(() -> channel.send().superOp(selectedUser));
        } else if (itemId == R.id.action_userlist_deadmin) {
            backgroundHandler.post(() -> channel.send().deSuperOp(selectedUser));
        } else if (itemId == R.id.action_userlist_voice) {
            backgroundHandler.post(() -> channel.send().voice(selectedUser));
        } else if (itemId == R.id.action_userlist_devoice) {
            backgroundHandler.post(() -> channel.send().deVoice(selectedUser));
        }
        return false;
    }

    @Override
    public void onUserListItemClicked(View view, User user) {
        selectedUser = user;
        showPopup(view);
    }
}
