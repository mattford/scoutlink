package uk.org.mattford.scoutlink.irc;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import com.google.common.collect.Lists;

import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ChannelInfoEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.HalfOpEvent;
import org.pircbotx.hooks.events.InviteEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.NickAlreadyInUseEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.OpEvent;
import org.pircbotx.hooks.events.OwnerEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.RemoveChannelBanEvent;
import org.pircbotx.hooks.events.RemoveChannelKeyEvent;
import org.pircbotx.hooks.events.RemoveChannelLimitEvent;
import org.pircbotx.hooks.events.RemoveInviteOnlyEvent;
import org.pircbotx.hooks.events.RemoveModeratedEvent;
import org.pircbotx.hooks.events.RemoveNoExternalMessagesEvent;
import org.pircbotx.hooks.events.RemovePrivateEvent;
import org.pircbotx.hooks.events.RemoveSecretEvent;
import org.pircbotx.hooks.events.RemoveTopicProtectionEvent;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.pircbotx.hooks.events.SetChannelBanEvent;
import org.pircbotx.hooks.events.SetChannelKeyEvent;
import org.pircbotx.hooks.events.SetChannelLimitEvent;
import org.pircbotx.hooks.events.SetInviteOnlyEvent;
import org.pircbotx.hooks.events.SetModeratedEvent;
import org.pircbotx.hooks.events.SetNoExternalMessagesEvent;
import org.pircbotx.hooks.events.SetPrivateEvent;
import org.pircbotx.hooks.events.SetSecretEvent;
import org.pircbotx.hooks.events.SetTopicProtectionEvent;
import org.pircbotx.hooks.events.SuperOpEvent;
import org.pircbotx.hooks.events.TopicEvent;
import org.pircbotx.hooks.events.UserListEvent;
import org.pircbotx.hooks.events.UserModeEvent;
import org.pircbotx.hooks.events.VersionEvent;
import org.pircbotx.hooks.events.VoiceEvent;
import org.pircbotx.hooks.events.WhoisEvent;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Channel;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Query;
import uk.org.mattford.scoutlink.model.Server;

public class IRCListener extends ListenerAdapter {

    private IRCService service;
    private Server server;

    public IRCListener(IRCService service) {
        super();
        this.service = service;
        this.server = service.getServer();
    }

    public void onNickAlreadyInUse(NickAlreadyInUseEvent event) {
        service.onNickAlreadyInUse();
        event.getBot().sendIRC().quitServer();
    }

    public void onConnect(ConnectEvent event) {
        event.getBot().sendIRC().listChannels();
        service.onConnect();
    }

    public void onDisconnect(DisconnectEvent event) {
        service.onDisconnect();
    }

    public void onHalfOp(HalfOpEvent event) {
        Message message;
        if (event.isHalfOp()) {
            message = new Message(service.getString(R.string.message_halfopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        } else {
            message = new Message(service.getString(R.string.message_dehalfopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        }
        server.getConversation(event.getChannel().getName()).addMessage(message);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onOp(OpEvent event) {
        Message message;
        if (event.isOp()) {
            message = new Message(service.getString(R.string.message_opevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        } else {
            message = new Message(service.getString(R.string.message_deopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        }
        server.getConversation(event.getChannel().getName()).addMessage(message);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSuperOp(SuperOpEvent event) {
        Message message;
        if (event.isSuperOp()) {
            message = new Message(service.getString(R.string.message_superopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        } else {
            message = new Message(service.getString(R.string.message_desuperopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        }
        server.getConversation(event.getChannel().getName()).addMessage(message);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onVoice(VoiceEvent event) {
        Message message;
        if (event.hasVoice()) {
            message = new Message(service.getString(R.string.message_voiceevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        } else {
            message = new Message(service.getString(R.string.message_devoiceevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        }
        server.getConversation(event.getChannel().getName()).addMessage(message);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onOwner(OwnerEvent event) {
        Message message;
        if (event.isOwner()) {
            message = new Message(service.getString(R.string.message_ownerevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        } else {
            message = new Message(service.getString(R.string.message_deownerevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()));
        }
        server.getConversation(event.getChannel().getName()).addMessage(message);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onMessage(MessageEvent event) {
        Message message = new Message(event.getUserHostmask().getNick(), event.getMessage());
        server.getConversation(event.getChannel().getName()).addMessage(message);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onAction(ActionEvent event) {
        //Message msg = new Message(service.getString(R.string.message_action, event.getUserHostmask().getNick(), event.getAction()));
        Message msg = new Message(event.getUserHostmask().getNick(), service.getString(R.string.message_action,event.getAction()));
        if (event.getChannel() != null) {
            server.getConversation(event.getChannel().getName()).addMessage(msg);
            service.onNewMessage(event.getChannel().getName());
        } else {
            // It's a private message.
            Conversation conversation = server.getConversation(event.getUserHostmask().getNick());
            if (conversation == null) {
                conversation = new Query(event.getUserHostmask().getNick());
                server.addConversation(conversation);
                Intent intent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", event.getUserHostmask().getNick());
                service.sendBroadcast(intent);
            }
            conversation.addMessage(msg);
            service.onNewMessage(event.getUserHostmask().getNick());
        }
    }

    public void onPrivateMessage(PrivateMessageEvent event) {
        Conversation conversation = server.getConversation(event.getUserHostmask().getNick());
        if (conversation == null) {
            conversation = new Query(event.getUserHostmask().getNick());
            server.addConversation(conversation);
            Intent intent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", event.getUserHostmask().getNick());
            service.sendBroadcast(intent);
        }
        Message msg = new Message(event.getUserHostmask().getNick(), event.getMessage());
        conversation.addMessage(msg);
        service.onNewMessage(event.getUserHostmask().getNick());
    }

    public void onNotice(NoticeEvent event) {
        String sender = event.getUserHostmask().getNick();
        Message message = new Message(service.getString(R.string.message_notice_sender, sender), event.getMessage());
        message.setBackgroundColour(service.getResources().getColor(R.color.light_green));
        message.setColour(service.getResources().getColor(R.color.white));
        if (event.getUser() != null) {
            for (String channel : getSharedChannels(event.getBot(), event.getUser())) {
                server.getConversation(channel).addMessage(message);
                service.onNewMessage(channel);
            }
        }
        server.getConversation("ScoutLink").addMessage(message);
        service.onNewMessage("ScoutLink");
    }

    public void onInvite(InviteEvent event) {
        Intent intent = new Intent().setAction(Broadcast.INVITE).putExtra("target", event.getChannel());
        service.sendBroadcast(intent);
    }

    public void onJoin(JoinEvent event) {
        if (event.getUserHostmask().getNick().equalsIgnoreCase(event.getBot().getNick())) {
            Conversation conv = new Channel(event.getChannel().getName());
            server.addConversation(conv);
            Intent intent = new Intent().setAction(Broadcast.NEW_CONVERSATION).putExtra("target", event.getChannel().getName());
            service.sendBroadcast(intent);
        } else {
            Message msg = new Message(service.getString(R.string.message_join, event.getUserHostmask().getNick(), event.getChannel().getName()));
            server.getConversation(event.getChannel().getName()).addMessage(msg);
            service.onNewMessage(event.getChannel().getName());
        }
    }

    public void onKick(KickEvent event) {
        if (event.getRecipientHostmask().getNick().equals(event.getBot().getNick())) {
            // We were kicked from a channel.
            Message msg = new Message(service.getString(R.string.message_kicked_self, event.getChannel().getName(), event.getUserHostmask().getNick(), event.getReason()));
            msg.setColour(Color.RED);
            server.getConversation("ScoutLink").addMessage(msg);
            service.onNewMessage("ScoutLink");
            server.removeConversation(event.getChannel().getName());
            Intent intent = new Intent().setAction(Broadcast.REMOVE_CONVERSATION).putExtra("target", event.getChannel().getName());
            service.sendBroadcast(intent);
        } else {
            Message msg = new Message(service.getString(R.string.message_kicked_other, event.getRecipientHostmask().getNick(), event.getChannel().getName(), event.getUserHostmask().getNick(), event.getReason()));
            server.getConversation(event.getChannel().getName()).addMessage(msg);
            service.onNewMessage(event.getChannel().getName());
        }
    }

    public void onNickChange(NickChangeEvent event) {
        Message msg = new Message(service.getString(R.string.message_nickchange, event.getOldNick(), event.getNewNick()));
        for (String channel : getSharedChannels(event.getBot(), event.getUser())) {
            server.getConversation(channel).addMessage(msg);
            service.onNewMessage(channel);
        }
    }

    public void onMotd(MotdEvent event) {
        Message msg = new Message(event.getMotd());
        server.getConversation("ScoutLink").addMessage(msg);
        service.onNewMessage("ScoutLink");
    }

    public void onPart(PartEvent event) {
        if (event.getBot().getNick().equals(event.getUserHostmask().getNick())) {
            // We left a channel.
            server.removeConversation(event.getChannel().getName());
            Intent intent = new Intent().setAction(Broadcast.REMOVE_CONVERSATION).putExtra("target", event.getChannel().getName());
            service.sendBroadcast(intent);
        } else {
            Message msg = new Message(service.getString(R.string.message_part, event.getUserHostmask().getNick(), event.getChannel().getName(), event.getReason()));
            server.getConversation(event.getChannel().getName()).addMessage(msg);
            service.onNewMessage(event.getChannel().getName());
        }
    }

   /* public void onPing(PingEvent event) {
        event.respond("Pong!");
    }*/

    public void onQuit(QuitEvent event) {
        if (event.getUserHostmask().getNick().equals(event.getBot().getNick())) {
            // We have quit, do nothing.
            return;
        }
        Message msg = new Message(service.getString(R.string.message_quit, event.getUserHostmask().getNick(), event.getReason()));
        for (String channel : getSharedChannels(event.getBot(), event.getUser())) {
            server.getConversation(channel).addMessage(msg);
            service.onNewMessage(channel);
        }
    }

    public void onSetChannelBan(SetChannelBanEvent event) {
        Message msg = new Message(service.getString(R.string.message_ban_add, event.getUserHostmask().getNick(), event.getBanHostmask().getHostmask()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemoveChannelBan(RemoveChannelBanEvent event) {
        Message msg = new Message(service.getString(R.string.message_ban_remove, event.getUserHostmask().getNick(), event.getHostmask().getHostmask()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSetPrivate(SetPrivateEvent event) {
        Message msg = new Message(service.getString(R.string.message_set_private, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemovePrivate(RemovePrivateEvent event) {
        Message msg = new Message(service.getString(R.string.message_unset_private, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSetSecret(SetSecretEvent event) {
        Message msg = new Message(service.getString(R.string.message_set_secret, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemoveSecret(RemoveSecretEvent event) {
        Message msg = new Message(service.getString(R.string.message_unset_secret, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSetChannelKey(SetChannelKeyEvent event) {
        Message msg = new Message(service.getString(R.string.message_set_key, event.getUserHostmask().getNick(), event.getKey()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemoveChannelKey(RemoveChannelKeyEvent event) {
        Message msg = new Message(service.getString(R.string.message_unset_key, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSetChannelLimit(SetChannelLimitEvent event) {
        Message msg = new Message(service.getString(R.string.message_set_limit, event.getUserHostmask().getNick(), Integer.toString(event.getLimit())));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemoveChannelLimit(RemoveChannelLimitEvent event) {
        Message msg = new Message(service.getString(R.string.message_unset_limit, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSetInviteOnly(SetInviteOnlyEvent event) {
        Message msg = new Message(service.getString(R.string.message_set_invite, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemoveInviteOnly(RemoveInviteOnlyEvent event) {
        Message msg = new Message(service.getString(R.string.message_unset_invite, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSetModerated(SetModeratedEvent event) {
        Message msg = new Message(service.getString(R.string.message_set_moderated, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemoveModerated(RemoveModeratedEvent event) {
        Message msg = new Message(service.getString(R.string.message_unset_moderated, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSetNoExternalMessages(SetNoExternalMessagesEvent event) {
        Message msg = new Message(service.getString(R.string.message_set_no_external, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemoveNoExternalMessages(RemoveNoExternalMessagesEvent event) {
        Message msg = new Message(service.getString(R.string.message_unset_no_external, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onSetTopicProtection(SetTopicProtectionEvent event) {
        Message msg = new Message(service.getString(R.string.message_set_topicprotect, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onRemoveTopicProtection(RemoveTopicProtectionEvent event) {
        Message msg = new Message(service.getString(R.string.message_unset_topicprotect, event.getUserHostmask().getNick()));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    @SuppressWarnings("unchecked")
    public void onUserList(UserListEvent event) {

        if (!event.isComplete()) {
            return;
        }
        String userList = "";

        for (User user : event.getUsers()) {
            userList = userList + " " + user.getNick();
        }
        Message msg = new Message("Users on channel: " + userList);
        msg.setColour(service.getResources().getColor(R.color.scoutlink_blue));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    @Override
    public void onVersion(VersionEvent event) {
        event.respond("VERSION " + service.getString(R.string.message_version));
    }

    public void onUserMode(UserModeEvent event) {
        String sender = event.getUserHostmask().getNick();
        String receiver = event.getRecipientHostmask().getNick();
        Message msg = new Message(service.getString(R.string.message_usermode, sender, event.getMode(), receiver));
        server.getConversation("ScoutLink").addMessage(msg);
        service.onNewMessage("ScoutLink");
    }

    public void onTopic(TopicEvent event) {
        Message msg;
        if (!event.isChanged()) {
            msg = new Message(service.getString(R.string.message_topic, event.getTopic(), event.getUser().getNick()));
        } else {
            msg = new Message(service.getString(R.string.message_topic_changed, event.getUser().getNick(), event.getTopic()));
        }
        msg.setColour(service.getResources().getColor(R.color.scoutlink_blue));
        server.getConversation(event.getChannel().getName()).addMessage(msg);
        service.onNewMessage(event.getChannel().getName());
    }

    public void onWhois(WhoisEvent event) {
        StringBuilder strB = new StringBuilder();
        for (String channel : event.getChannels()) {
            strB.append(channel + " ");
        }
        Message msg = new Message(
                service.getString(R.string.message_whois,
                        event.getNick(),
                        event.getLogin(),
                        event.getHostname(),
                        event.getRealname(),
                        event.getServer(),
                        event.getAwayMessage(),
                        event.getRegisteredAs(),
                        strB.toString()
                        )
        );
        server.getConversation(service.getString(R.string.server_window_title)).addMessage(msg);
        service.onNewMessage(service.getString(R.string.server_window_title));
    }

    @SuppressWarnings("unchecked")
    public void onChannelInfo(ChannelInfoEvent event) {
        server.setChannelList(event.getList());
    }

    public void onServerPing(ServerPingEvent event) {
        //event.respond(event.getResponse());
    }

    public ArrayList<String> getSharedChannels(PircBotX bot, User user) {
        ArrayList<String> channels = new ArrayList<>();
        for (org.pircbotx.Channel userChan : user.getChannels()) {
            if (bot.getUserBot().getChannels().contains(userChan)) {
                channels.add(userChan.getName());
            }
        }
        return channels;
    }


}
