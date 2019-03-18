package com.examper.chatyoxmpp.android.main.xmpp;


import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.examper.chatyoxmpp.android.main.constants.ChatListDbColumns;
import com.examper.chatyoxmpp.android.main.constants.UserChatDbColumns;

/***This is custom provider class. We are handling database
 *  related work Like creating tables and writing queries*/
public class ChatProvider extends ContentProvider {

    public static final String AUTHORITY = "com.examper.chatyoxmpp.android.main.Database.chat";
    public static final String TABLE_NAME_USER_CHAT = "userchats";
    public static final String TABLE_NAME_USER_LIST = "userList";


    public static final Uri CONTENT_URI_USER_LIST = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_NAME_USER_LIST);

    public static final Uri CONTENT_URI_USER_CHAT = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_NAME_USER_CHAT);

    private static final UriMatcher URI_MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);


    private static final int MESSAGES_USER = 3;
    private static final int MESSAGE_ID_USER = 4;

    private static final int MESSAGES_USER_LIST = 5;
    private static final int MESSAGE_ID_USER_LIST = 6;


    static {

        URI_MATCHER.addURI(AUTHORITY, TABLE_NAME_USER_CHAT, MESSAGES_USER);
        URI_MATCHER.addURI(AUTHORITY, TABLE_NAME_USER_CHAT + "/#", MESSAGE_ID_USER);

        URI_MATCHER.addURI(AUTHORITY, TABLE_NAME_USER_LIST, MESSAGES_USER_LIST);
        URI_MATCHER.addURI(AUTHORITY, TABLE_NAME_USER_LIST + "/#", MESSAGE_ID_USER_LIST);


    }

    private static final String TAG = "Amiggo.ChatProvider";

    private SQLiteOpenHelper mOpenHelper;

    public ChatProvider() {
    }

    public ChatProvider(Context context) {
        mOpenHelper = new ChatDatabaseHelper(context);

    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String segment = "";
        switch (URI_MATCHER.match(url)) {

            case MESSAGES_USER_LIST:
                count = db.delete(TABLE_NAME_USER_LIST, where, whereArgs);
                break;
            case MESSAGE_ID_USER_LIST:
                segment = url.getPathSegments().get(1);

                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + segment;
                } else {
                    where = "_id=" + segment + " AND (" + where + ")";
                }

                count = db.delete(TABLE_NAME_USER_LIST, where, whereArgs);
                break;
            case MESSAGES_USER:
                count = db.delete(TABLE_NAME_USER_CHAT, where, whereArgs);
                break;
            case MESSAGE_ID_USER:
                segment = url.getPathSegments().get(1);

                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + segment;
                } else {
                    where = "_id=" + segment + " AND (" + where + ")";
                }

                count = db.delete(TABLE_NAME_USER_CHAT, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public String getType(Uri url) {
        int match = URI_MATCHER.match(url);
        switch (match) {
            case MESSAGES_USER:
                return UserChatDbColumns.CONTENT_TYPE;
            case MESSAGE_ID_USER:
                return UserChatDbColumns.CONTENT_ITEM_TYPE;
            case MESSAGES_USER_LIST:
                return ChatListDbColumns.CONTENT_TYPE;
            case MESSAGE_ID_USER_LIST:
                return ChatListDbColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {

        int match = URI_MATCHER.match(url);
        String TABLENAME = "";
        Uri CONTENTURI;
        switch (match) {

            case MESSAGES_USER:
                TABLENAME = TABLE_NAME_USER_CHAT;
                CONTENTURI = CONTENT_URI_USER_CHAT;
                break;

            case MESSAGES_USER_LIST:
                TABLENAME = TABLE_NAME_USER_LIST;
                CONTENTURI = CONTENT_URI_USER_LIST;
                break;
            default:
                throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }


        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();


        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insert(TABLENAME, null, values);

        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + url);
        }

        Uri noteUri = ContentUris.withAppendedId(CONTENTURI, rowId);
        getContext().getContentResolver().notifyChange(noteUri, null);
        return noteUri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ChatDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        int match = URI_MATCHER.match(url);
        String orderBy;

        switch (match) {

            case MESSAGES_USER:
                qBuilder.setTables(TABLE_NAME_USER_CHAT);

                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = UserChatDbColumns.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case MESSAGE_ID_USER:
                qBuilder.setTables(TABLE_NAME_USER_CHAT);
                qBuilder.appendWhere("_id=");
                qBuilder.appendWhere(url.getPathSegments().get(1));
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = UserChatDbColumns.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;

            case MESSAGES_USER_LIST:
                qBuilder.setTables(TABLE_NAME_USER_LIST);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = ChatListDbColumns.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            case MESSAGE_ID_USER_LIST:
                qBuilder.setTables(TABLE_NAME_USER_LIST);
                qBuilder.appendWhere("_id=");
                qBuilder.appendWhere(url.getPathSegments().get(1));
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = ChatListDbColumns.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }


        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qBuilder.query(db, projectionIn, selection, selectionArgs, null, null, orderBy);
        if (ret == null) {
            infoLog("ChatProvider.query: failed");
        } else {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }

        return ret;
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = URI_MATCHER.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String segment = "";
        switch (match) {

            case MESSAGES_USER:
                count = db.update(TABLE_NAME_USER_CHAT, values, where, whereArgs);
                break;
            case MESSAGE_ID_USER:
                segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update(TABLE_NAME_USER_CHAT, values, "_id=" + rowId, null);
                break;

            case MESSAGES_USER_LIST:
                count = db.update(TABLE_NAME_USER_LIST, values, where, whereArgs);
                break;
            case MESSAGE_ID_USER_LIST:
                segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update(TABLE_NAME_USER_LIST, values, "_id=" + rowId, null);
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URL: " + url);
        }

        infoLog("*** notifyChange() rowId: " + rowId + " url " + url);
        getContext().getContentResolver().notifyChange(url, null);
        return count;

    }

    private static void infoLog(String data) {
        Log.i(TAG, data);
    }

    private static class ChatDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "Chatyo.db";
        private static final int DATABASE_VERSION = 1;

        public ChatDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        
        @Override
        public void onCreate(SQLiteDatabase db) {
            infoLog("creating new chat table");
            db.execSQL("CREATE TABLE " + TABLE_NAME_USER_CHAT + " (" + UserChatDbColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + UserChatDbColumns.DATE + " INTEGER,"
                    + UserChatDbColumns.DIRECTION + " INTEGER,"
                    + UserChatDbColumns.PACKET_ID + " TEXT,"
                    + UserChatDbColumns.BODY + " TEXT,"
                    + UserChatDbColumns.FRIEND_JID + " TEXT,"
                    + UserChatDbColumns.LOGIN_USER_JID + " TEXT,"
                    + UserChatDbColumns.SENDER_NAME + " TEXT,"
                    + UserChatDbColumns.MESSAGE_SUBJECT + " TEXT);");

            db.execSQL("CREATE TABLE " + TABLE_NAME_USER_LIST + " (" + ChatListDbColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ChatListDbColumns.DATE + " INTEGER,"
                    + ChatListDbColumns.UNREAD_MSG_COUNT + " INTEGER default 0,"
                    + ChatListDbColumns.FRIEND_JID + " TEXT,"
                    + ChatListDbColumns.FRIEND_NAME + " TEXT,"
                    + ChatListDbColumns.LOGIN_USER_JID + " TEXT,"
                    + ChatListDbColumns.LAST_MESSAGE + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            infoLog("onUpgrade: from " + oldVersion + " to " + newVersion);

        }

    }


}
