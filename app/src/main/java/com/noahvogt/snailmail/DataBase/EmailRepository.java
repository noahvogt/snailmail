package com.noahvogt.snailmail.DataBase;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import static com.noahvogt.snailmail.MainActivity.userGlobal;

public class EmailRepository {

    private MessageDao messageDao;
    private final LiveData<List<Message>> mDraftLiveMessage;
    private LiveData<List<Message>> mInboxLiveMessage;
    private LiveData<List<Message>> mSentLiveMessage;
    private LiveData<List<Message>> mArchiveLiveMessage;
    private LiveData<List<Message>> mSpamLiveMessage;

    private List<Message> mAllMessages;


    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    /*get all messages sorted by date out of Database*/
    public EmailRepository(Application application) {
        EmailRoomDatabase db = EmailRoomDatabase.getDatabase(application);
        messageDao = db.messageDao();
        mInboxLiveMessage = messageDao.getLiveInboxMessages(userGlobal);
        mDraftLiveMessage = messageDao.getLiveDraftMessages(userGlobal);
        mArchiveLiveMessage = messageDao.getLiveArchiveMessages(userGlobal);
        mSentLiveMessage = messageDao.getLiveSentMessages(userGlobal);
        mSpamLiveMessage = messageDao.getLiveSpamMessages(userGlobal);
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    /* returns all messages of with tag Draft */
    public LiveData<List<Message>> getDraftMessages() {
        return mDraftLiveMessage;
    }

    public LiveData<List<Message>> getSpamMessage(){return mSpamLiveMessage;}

    public LiveData<List<Message>> getInboxMessages() {
        return mInboxLiveMessage;
    }

    public LiveData<List<Message>> getSentMessages() {
        return mSentLiveMessage;
    }

    public LiveData<List<Message>> getArchiveMessages() {
        return mArchiveLiveMessage;
    }

    /*problems with main Thread */
    public List<Message> getAllMessages(){
        //mAllMessages = messageDao.getAllMessages();
        return mAllMessages;}



    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Message message) {
        EmailRoomDatabase.databaseWriteExecutor.execute(() -> {
            messageDao.insert(message);
        });
    }

    public void deleteMessage(final Message message){
        EmailRoomDatabase.databaseWriteExecutor.execute(() -> {
            messageDao.delete(message);
        });
    }

    public void updateFolder(final int id, String folder){
        EmailRoomDatabase.databaseWriteExecutor.execute(() -> {
            messageDao.updateFolder(id ,folder);
        });
    }

    public void updateDate(final int id, final String date){
        EmailRoomDatabase.databaseWriteExecutor.execute(() -> {
            messageDao.updateDate(id, date);
        });
    }
}