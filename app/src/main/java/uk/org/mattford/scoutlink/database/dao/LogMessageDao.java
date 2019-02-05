package uk.org.mattford.scoutlink.database.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import uk.org.mattford.scoutlink.database.entities.LogMessage;

@Dao
public interface LogMessageDao {

    @Query("SELECT * FROM log_messages WHERE conversation_name = :conversationName ORDER BY message_date ASC")
    LiveData<List<LogMessage>> findByConversation(String conversationName);

    @Query("SELECT * FROM log_messages WHERE conversation_name = :conversationName ORDER BY message_date ASC")
    List<LogMessage> findByConversationSync(String conversationName);

    @Query("SELECT conversation_name FROM log_messages WHERE conversation_type != 2 GROUP BY conversation_name")
    LiveData<List<LogMessage>> findConversationNamesExcludingServerWindow();

    @Query("SELECT * FROM log_messages WHERE conversation_name = :conversationName ORDER BY message_date DESC LIMIT :limit")
    List<LogMessage> findConversationMessagesWithLimit(String conversationName, Integer limit);

    @Insert
    void insert(LogMessage message);

    @Query("DELETE FROM log_messages WHERE conversation_name = :conversationName")
    int deleteByConversation(String conversationName);
}
