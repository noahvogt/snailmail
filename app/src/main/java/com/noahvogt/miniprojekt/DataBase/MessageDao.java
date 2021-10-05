package com.noahvogt.miniprojekt.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MessageDao {

    // TODO: updating messages, search funktion, read in cc and bcc as list
    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Message message);

    @Query("DELETE FROM message_table")
    void deleteAll();

    @Delete(entity = Message.class)
    void delete(Message message);

    @Query("UPDATE message_table SET folder = :folder WHERE id = :id")
    void updateMessage(int id, String folder);

    @Query("DELETE FROM message_table WHERE subject='DELETE'")
    void deleteNewMessage();


    @Query("SELECT * FROM message_table")
    LiveData<List<Message>> getAllMessages();

    /*gets messages all messages ordered by date
    * !IMPORTANT I don't know in which direction */
    @Query("SELECT * FROM message_table ORDER BY date ASC")
    LiveData<List<Message>> getDateMessages();

    /* get Draft messages*/
    @Query("SELECT * FROM message_table WHERE folder LIKE 'Draft' ORDER BY date DESC")
    LiveData<List<Message>> getDraftMessages();

    /* get Inbox messages*/
    @Query("SELECT * FROM message_table WHERE folder LIKE 'Inbox' ORDER BY date DESC")
    LiveData<List<Message>> getInboxMessages();

    /* get Sent messages*/
    @Query("SELECT * FROM message_table WHERE folder LIKE 'Sent' ORDER BY date DESC")
    LiveData<List<Message>> getSentMessages();

    /* get Archive messages*/
    @Query("SELECT * FROM message_table WHERE folder LIKE 'Archive' ORDER BY date DESC")
    LiveData<List<Message>> getArchiveMessages();

    /* get Spam messages*/
    @Query("SELECT * FROM message_table WHERE folder LIKE 'Spam' ORDER BY date DESC")
    LiveData<List<Message>> getSpamMessages();


}