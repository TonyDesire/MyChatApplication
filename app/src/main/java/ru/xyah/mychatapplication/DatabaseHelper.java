package ru.xyah.mychatapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by XYAH on 15.04.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    private static final String DATABASE_NAME = "dbLocal.db";
    private static final String DATABASE_TABLE = "table_name";
    private static final int DATABASE_VERSION = 1;

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS ClientLogInData (_id integer primary key autoincrement, login, password)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Chat (_id integer primary key autoincrement, author_id, client_id, data, text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Users (_id integer primary key autoincrement, first_name, second_name, age, photo_path, middle_name, nationality, state, city, address, email, gender, faculty, phone_number)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }




    public void clearChatTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM Chat");
        db.close();
    }

    public void clearClientLogInDataTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM ClientLogInData");
        db.close();
    }
}
