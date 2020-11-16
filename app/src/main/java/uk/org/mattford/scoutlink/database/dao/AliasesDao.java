package uk.org.mattford.scoutlink.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import uk.org.mattford.scoutlink.database.entities.Alias;

@Dao
public interface AliasesDao {

    @Query("SELECT * FROM aliases")
    LiveData<List<Alias>> getAliases();

    @Insert
    void insert(Alias alias);

    @Query("DELETE FROM aliases WHERE command_name = :commandName")
    int deleteAlias(String commandName);
}
