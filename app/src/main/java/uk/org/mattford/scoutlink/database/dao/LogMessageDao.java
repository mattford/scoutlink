package uk.org.mattford.scoutlink.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import uk.org.mattford.scoutlink.database.entities.LogMessage;

@Dao
public interface LogMessageDao {

    @Query("SELECT * FROM log_messages WHERE conversation_name = :conversationName ORDER BY message_date DESC")
    List<LogMessage> findByConversation(String conversationName);

    @Insert
    void insert(LogMessage message);

    @Query("DELETE FROM log_messages WHERE conversation_name = :conversationName")
    void deleteByConversation(String conversationName);
}
