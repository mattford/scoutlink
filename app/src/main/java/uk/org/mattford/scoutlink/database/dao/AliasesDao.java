package uk.org.mattford.scoutlink.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import uk.org.mattford.scoutlink.database.entities.Alias;

@Dao
public interface AliasesDao {

    @Query("SELECT * FROM aliases")
    LiveData<List<Alias>> getAliases();

    @Query("SELECT * FROM aliases WHERE command_name = :commandName")
    LiveData<Alias> getAlias(String commandName);

    @Query("SELECT * FROM aliases WHERE command_name = :commandName")
    Alias getAliasSync(String commandName);

    @Insert
    void insertAlias(Alias alias);

    @Update
    void updateAlias(Alias alias);

    @Query("DELETE FROM aliases WHERE command_name = :commandName")
    int deleteAlias(String commandName);
}
