package uk.org.mattford.scoutlink.irc;

import android.content.Intent;

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
import org.pircbotx.hooks.events.ServerResponseEvent;
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
import uk.org.mattford.scoutlink.event.JoinFailedEvent;
import uk.org.mattford.scoutlink.event.MessageNotSentEvent;
import uk.org.mattford.scoutlink.event.NotifyEvent;
import uk.org.mattford.scoutlink.model.Broadcast;
import uk.org.mattford.scoutlink.model.Channel;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Query;
import uk.org.mattford.scoutlink.model.Server;

public class IRCListener extends ListenerAdapter {

    private final IRCService service;
    private final Server server;

    IRCListener(IRCService service) {
        super();
        this.service = service;
        this.server = service.getServer();
    }

    public void onServerResponse(ServerResponseEvent event) {
        switch (event.getCode()) {
            case 604:
            case 600:
                // User on watchlist joined IRC
                NotifyEvent onlineEvent = new NotifyEvent(event.getParsedResponse().get(1), NotifyEvent.TYPE_ONLINE, false, true, false, event.getParsedResponse().get(4));
                onNotify(onlineEvent);
                break;
            case 605:
            case 601:
                // User on watchlist left IRC
                NotifyEvent offlineEvent = new NotifyEvent(event.getParsedResponse().get(1), NotifyEvent.TYPE_ONLINE, false, false, false, event.getParsedResponse().get(4));
                onNotify(offlineEvent);
                break;
            case 602:
                // Removed from list
                NotifyEvent removedEvent = new NotifyEvent(event.getParsedResponse().get(1), NotifyEvent.TYPE_MANAGELIST, false, false, false, event.getParsedResponse().get(4));
                onNotify(removedEvent);
                break;
            case 598:
                // User on watch list is away
                NotifyEvent awayEvent = new NotifyEvent(event.getParsedResponse().get(1), NotifyEvent.TYPE_AWAY, true, true, false, event.getParsedResponse().get(4));
                onNotify(awayEvent);
                break;
            case 599:
                // User on watch list is back from away
                NotifyEvent backEvent = new NotifyEvent(event.getParsedResponse().get(1), NotifyEvent.TYPE_AWAY, false, true, false, event.getParsedResponse().get(4));
                onNotify(backEvent);
                break;
            case 474:
            case 473:
            case 475:
                JoinFailedEvent jfEvent = new JoinFailedEvent(event.getParsedResponse().get(1), event.getParsedResponse().get(2));
                onJoinFailed(jfEvent);
                break;
            case 404:
                MessageNotSentEvent mnsEvent = new MessageNotSentEvent(event.getParsedResponse().get(1), event.getParsedResponse().get(2));
                onMessageNotSent(mnsEvent);
                break;
        }
    }

    private void onMessageNotSent(MessageNotSentEvent event) {
        Message msg = new Message(event.getMessage(), Message.SENDER_TYPE_SERVER, Message.TYPE_ERROR);
        server.getConversation(event.getChannel()).addMessage(msg);
    }

    private void onJoinFailed(JoinFailedEvent event) {
        String message = service.getString(R.string.message_join_failed, event.getChannel(), event.getMessage());
        service.sendToast(message);
    }

    private void onNotify(NotifyEvent event) {
        String text = "";
        switch (event.getType()) {
            case NotifyEvent.TYPE_ONLINE:
                if (event.isOnline()) {
                    text = service.getString(R.string.message_notify_online, event.getNick());
                } else {
                    text = service.getString(R.string.message_notify_offline, event.getNick());
                }
                service.sendToast(text);
                break;
            case NotifyEvent.TYPE_MANAGELIST:
                if (event.isAdded()) {
                    text = service.getString(R.string.message_notify_added, event.getNick());
                } else {
                    text = service.getString(R.string.message_notify_removed, event.getNick());
                }
                break;
            case NotifyEvent.TYPE_AWAY:
                if (event.isAway()) {
                    text = service.getString(R.string.message_notify_away, event.getNick(), event.getMessage());
                } else {
                    text = service.getString(R.string.message_notify_back, event.getNick());
                }
                service.sendToast(text);
                break;
        }
        Message msg = new Message(text, Message.SENDER_TYPE_SERVER, Message.TYPE_EVENT);
        server.getConversation(service.getString(R.string.server_window_title)).addMessage(msg);
        service.onNotify(event);
    }

    public void onNickAlreadyInUse(NickAlreadyInUseEvent event) {
        service.sendToast(service.getString(R.string.nick_already_in_use));
        event.getBot().sendIRC().quitServer();
    }

    public void onConnect(ConnectEvent event) {
        service.onConnect();
    }

    public void onDisconnect(DisconnectEvent event) {
        service.onDisconnect();
    }

    public void onHalfOp(HalfOpEvent event) {
        Message message;
        if (event.isHalfOp()) {
            message = new Message(
                service.getString(R.string.message_halfopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        } else {
            message = new Message(
                service.getString(R.string.message_dehalfopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        }
        Conversation conversation = server.getConversation(event.getChannel().getName());
        conversation.addMessage(message);
        conversation.onUserListChanged();
    }

    public void onOp(OpEvent event) {
        Message message;
        if (event.isOp()) {
            message = new Message(
                service.getString(R.string.message_opevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        } else {
            message = new Message(
                service.getString(R.string.message_deopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        }
        Conversation conversation = server.getConversation(event.getChannel().getName());
        conversation.addMessage(message);
        conversation.onUserListChanged();
    }

    public void onSuperOp(SuperOpEvent event) {
        Message message;
        if (event.isSuperOp()) {
            message = new Message(
                service.getString(R.string.message_superopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        } else {
            message = new Message(
                service.getString(R.string.message_desuperopevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        }
        Conversation conversation = server.getConversation(event.getChannel().getName());
        conversation.addMessage(message);
        conversation.onUserListChanged();
    }

    public void onVoice(VoiceEvent event) {
        Message message;
        if (event.hasVoice()) {
            message = new Message(
                service.getString(R.string.message_voiceevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        } else {
            message = new Message(
                service.getString(R.string.message_devoiceevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        }
        Conversation conversation = server.getConversation(event.getChannel().getName());
        conversation.addMessage(message);
        conversation.onUserListChanged();
    }

    public void onOwner(OwnerEvent event) {
        Message message;
        if (event.isOwner()) {
            message = new Message(
                service.getString(R.string.message_ownerevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        } else {
            message = new Message(
                service.getString(R.string.message_deownerevent, event.getUserHostmask().getNick(), event.getRecipientHostmask().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        }
        Conversation conversation = server.getConversation(event.getChannel().getName());
        conversation.addMessage(message);
        conversation.onUserListChanged();
    }

    public void onMessage(MessageEvent event) {
        Message message = new Message(event.getUserHostmask().getNick(), event.getMessage(), Message.SENDER_TYPE_OTHER, Message.TYPE_MESSAGE);
        server.getConversation(event.getChannel().getName()).addMessage(message);
    }

    public void onAction(ActionEvent event) {
        Message msg = new Message(
            event.getUserHostmask().getNick(),
            event.getAction(),
            Message.SENDER_TYPE_OTHER,
            Message.TYPE_ACTION
        );
        if (event.getChannel() != null) {
            server.getConversation(event.getChannel().getName()).addMessage(msg);
        } else {
            // It's a private message.
            Conversation conversation = server.getConversation(event.getUserHostmask().getNick());
            if (conversation == null) {
                conversation = new Query(event.getUserHostmask().getNick());
                server.addConversation(conversation);
            }
            conversation.addMessage(msg);
        }
    }

    public void onPrivateMessage(PrivateMessageEvent event) {
        String nickname = event.getUserHostmask().getNick();
        if (service.isUserBlocked(nickname)) {
            // User is blocked, ignore message event completely.
            return;
        }
        Conversation conversation = server.getConversation(nickname);
        if (conversation == null) {
            conversation = new Query(event.getUserHostmask().getNick());
            server.addConversation(conversation);
        }
        Message msg = new Message(
            event.getUserHostmask().getNick(),
            event.getMessage(),
            Message.SENDER_TYPE_OTHER,
            Message.TYPE_MESSAGE
        );
        conversation.addMessage(msg);
    }

    public void onNotice(NoticeEvent event) {
        String sender = event.getUserHostmask().getNick();
        Message message;
        if (sender.contains(".scoutlink.net")) {
            // Treat as a server message
            message = new Message(event.getMessage(), Message.SENDER_TYPE_SERVER, Message.TYPE_SERVER);
        } else {
            message = new Message(sender, event.getMessage(), Message.SENDER_TYPE_OTHER, Message.TYPE_NOTICE);
            if (event.getUser() != null) {
                ArrayList<String> sharedConversations = getSharedChannels(event.getBot(), event.getUser());
                for (String channel : sharedConversations) {
                    server.getConversation(channel).addMessage(message);
                }
            }
        }

        // Add to Server Window
        server.getConversation(service.getString(R.string.server_window_title)).addMessage(message);
    }

    public void onInvite(InviteEvent event) {
        Intent intent = new Intent().setAction(Broadcast.INVITE).putExtra("target", event.getChannel());
        service.sendBroadcast(intent);
    }

    public void onUserList(UserListEvent event) {
        Conversation conversation = server.getConversation(event.getChannel().getName());
        conversation.onUserListChanged();
    }

    public void onJoin(JoinEvent event) {
        Conversation conversation;
        if (event.getUserHostmask().getNick().equalsIgnoreCase(event.getBot().getNick())) {
            conversation = new Channel(event.getChannel().getName(), event.getChannel());
            server.addConversation(conversation, true);
            service.loadLoggedMessages(conversation);
            conversation.addMessage(
                new Message(
                    String.format("You joined %s", conversation.getName()),
                    Message.SENDER_TYPE_SERVER,
                    Message.TYPE_EVENT
                )
            );
        } else {
            Message msg = new Message(
                service.getString(R.string.message_join, event.getUserHostmask().getNick(), event.getChannel().getName()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
            conversation = server.getConversation(event.getChannel().getName());
            conversation.addMessage(msg);
        }
        conversation.onUserListChanged();
    }

    public void onKick(KickEvent event) {
        if (event.getRecipientHostmask().getNick().equals(event.getBot().getNick())) {
            // We were kicked from a channel.
            Message msg = new Message(
                service.getString(R.string.message_kicked_self, event.getChannel().getName(), event.getUserHostmask().getNick(), event.getReason()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
            server.getConversation(service.getString(R.string.server_window_title)).addMessage(msg);
            server.removeConversation(event.getChannel().getName());
        } else {
            Message msg = new Message(
                service.getString(R.string.message_kicked_other, event.getRecipientHostmask().getNick(), event.getChannel().getName(), event.getUserHostmask().getNick(), event.getReason()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
            Conversation conversation = server.getConversation(event.getChannel().getName());
            conversation.addMessage(msg);
            conversation.onUserListChanged();
        }
    }

    public void onNickChange(NickChangeEvent event) {
        if (event.getUser() == null) {
            return;
        }
        Message msg = new Message(
            service.getString(R.string.message_nickchange, event.getOldNick(), event.getNewNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        for (String channel : getSharedChannels(event.getBot(), event.getUser())) {
            Conversation conversation = server.getConversation(channel);
            conversation.addMessage(msg);
            conversation.onUserListChanged();
        }
    }

    public void onMotd(MotdEvent event) {
        Message msg = new Message(event.getMotd(), Message.SENDER_TYPE_SERVER, Message.TYPE_SERVER);
        server.getConversation(service.getString(R.string.server_window_title)).addMessage(msg);
    }

    public void onPart(PartEvent event) {
        if (event.getBot().getNick().equals(event.getUserHostmask().getNick())) {
            // We left a channel.
            server.removeConversation(event.getChannel().getName());
        } else {
            Message msg = new Message(
                service.getString(R.string.message_part, event.getUserHostmask().getNick(), event.getChannel().getName(), event.getReason()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
            Conversation conversation = server.getConversation(event.getChannelName());
            conversation.addMessage(msg);
            conversation.onUserListChanged();
        }
    }

    public void onQuit(QuitEvent event) {
        if (event.getUserHostmask().getNick().equals(event.getBot().getNick())) {
            // We have quit, do nothing.
            return;
        }
        Message msg = new Message(
            service.getString(R.string.message_quit, event.getUserHostmask().getNick(), event.getReason()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        for (String channel : getSharedChannels(event.getBot(), event.getUser())) {
            Conversation conversation = server.getConversation(channel);
            conversation.addMessage(msg);
            conversation.onUserListChanged();
        }
    }

    public void onSetChannelBan(SetChannelBanEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_ban_add, event.getUserHostmask().getNick(), event.getBanHostmask().getHostmask()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemoveChannelBan(RemoveChannelBanEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_ban_remove, event.getUserHostmask().getNick(), event.getHostmask().getHostmask()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onSetPrivate(SetPrivateEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_set_private, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemovePrivate(RemovePrivateEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_unset_private, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onSetSecret(SetSecretEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_set_secret, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemoveSecret(RemoveSecretEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_unset_secret, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onSetChannelKey(SetChannelKeyEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_set_key, event.getUserHostmask().getNick(), event.getKey()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemoveChannelKey(RemoveChannelKeyEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_unset_key, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onSetChannelLimit(SetChannelLimitEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_set_limit, event.getUserHostmask().getNick(), Integer.toString(event.getLimit())),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemoveChannelLimit(RemoveChannelLimitEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_unset_limit, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onSetInviteOnly(SetInviteOnlyEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_set_invite, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemoveInviteOnly(RemoveInviteOnlyEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_unset_invite, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onSetModerated(SetModeratedEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_set_moderated, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemoveModerated(RemoveModeratedEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_unset_moderated, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onSetNoExternalMessages(SetNoExternalMessagesEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_set_no_external, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemoveNoExternalMessages(RemoveNoExternalMessagesEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_unset_no_external, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onSetTopicProtection(SetTopicProtectionEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_set_topicprotect, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onRemoveTopicProtection(RemoveTopicProtectionEvent event) {
        Message msg = new Message(
            service.getString(R.string.message_unset_topicprotect, event.getUserHostmask().getNick()),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    @Override
    public void onVersion(VersionEvent event) {
        event.respond("VERSION " + service.getString(R.string.message_version));
    }

    public void onUserMode(UserModeEvent event) {
        String sender = event.getUserHostmask().getNick();
        String receiver = event.getRecipientHostmask().getNick();

        Message msg = new Message(
            service.getString(R.string.message_usermode, sender, event.getMode(), receiver),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(service.getString(R.string.server_window_title)).addMessage(msg);
    }

    public void onTopic(TopicEvent event) {
        Message msg;
        if (!event.isChanged()) {
            msg = new Message(
                service.getString(R.string.message_topic, event.getTopic(), event.getUser().getNick()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        } else {
            msg = new Message(
                service.getString(R.string.message_topic_changed, event.getUser().getNick(), event.getTopic()),
                Message.SENDER_TYPE_SERVER,
                Message.TYPE_EVENT
            );
        }
        server.getConversation(event.getChannel().getName()).addMessage(msg);
    }

    public void onWhois(WhoisEvent event) {
        StringBuilder channelsBuilder = new StringBuilder();
        for (String channel : event.getChannels()) {
            channelsBuilder.append(channel).append(" ");
        }
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(
            service.getString(R.string.message_whois_fullhost,
                    event.getNick(),
                    event.getLogin(),
                    event.getHostname(),
                    event.getRealname()
            )
        );
        if (!channelsBuilder.toString().equals("")) {
            messageBuilder.append(service.getString(R.string.message_whois_channels, channelsBuilder.toString()));
        }
        if (event.getAwayMessage() != null) {
            messageBuilder.append(service.getString(R.string.message_whois_away, event.getAwayMessage()));
        }
        if (event.getIdleSeconds() != 0) {
            messageBuilder.append(service.getString(R.string.message_whois_idle, event.getIdleSeconds()));
        }
        if (event.getServer() != null) {
            messageBuilder.append(service.getString(R.string.message_whois_server, event.getServer() + " " + event.getServerInfo()));
        }
        if (event.getRegisteredAs() != null) {
            messageBuilder.append(service.getString(R.string.message_whois_registered, event.getRegisteredAs()));
        }
        Message msg = new Message(
            messageBuilder.toString(),
            Message.SENDER_TYPE_SERVER,
            Message.TYPE_EVENT
        );
        server.getConversation(service.getString(R.string.server_window_title)).addMessage(msg);
    }

    public void onChannelInfo(ChannelInfoEvent event) {
        server.setChannelList(new ArrayList<>(event.getList()));
        Intent intent = new Intent(Broadcast.CHANNEL_LIST_INFO);
        service.sendBroadcast(intent);
    }

    public void onServerPing(ServerPingEvent event) {
        //event.respond(event.getResponse());
    }

    private ArrayList<String> getSharedChannels(PircBotX bot, User user) {
        ArrayList<String> channels = new ArrayList<>();
        for (org.pircbotx.Channel channel : bot.getUserChannelDao().getChannels(user)) {
            channels.add(channel.getName());
        }
        return channels;
    }
}
