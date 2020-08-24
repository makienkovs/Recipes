package com.makienkovs.recipes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_SQLITE = "data.sqlite";
    public static final int DB_VERSION = 13;
    public static final String MYTABLE = "MYTABLE";
    public static final String ID = "ID";
    public static final String USER = "USER";
    public static final String TITLE = "TITLE";
    public static final String INGREDIENTS = "INGREDIENTS";
    public static final String HOWTOCOOK = "HOWTOCOOK";

    public DBHelper(Context context) {
        super(context, DB_SQLITE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE MYTABLE (ID TEXT, USER TEXT, TITLE TEXT, INGREDIENTS TEXT, HOWTOCOOK TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS MYTABLE;");
        onCreate(db);
    }
}