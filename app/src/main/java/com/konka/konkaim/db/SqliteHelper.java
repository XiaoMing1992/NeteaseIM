package com.konka.konkaim.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by HP on 2018-6-5.
 */

public class SqliteHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "konkaim.db";
    private final static int DB_VERSION = 1;

    private final static String CREATE_TABLE = "create table if not exists"
            + " " + DBField.TABLE_NAME
            + "(" + DBField.ID + " " + "integer primary key autoincrement"
            + "," + DBField.MY_ACCOUNT + " " + "varchar(200) not null"
            + "," + DBField.FRIEND_ACCOUNT + " " + "varchar(200) null"
//            + "," + DBField.TEAM_ID + " " + "varchar(200) null"
            + "," + DBField.FROM + " " + "varchar(200) null"
            + "," + DBField.TO + " " + "varchar(200) null"
            + "," + DBField.IS_CONNECT + " " + "integer default 0"
//            + "," + DBField.IS_TEAM + " " + "integer default 0"
            + "," + DBField.IS_FRIEND + " " + "integer default 0"
            + "," + DBField.IS_OUT + " " + "integer default 0"
            + "," + DBField.TIME + " " + "varchar(50) null" + ");";

    private final static String CREATE_TEAM_TABLE = "create table if not exists"
            + " " + TeamDBField.TABLE_NAME
            + "(" + TeamDBField.ID + " " + "integer primary key autoincrement"
            + "," + TeamDBField.MY_ACCOUNT + " " + "varchar(200) not null"
            + "," + TeamDBField.TEAM_ID + " " + "varchar(200) null"
            + "," + TeamDBField.TEAM_NAME + " " + "varchar(200) null"
            + "," + TeamDBField.TIME + " " + "varchar(50) null" + ");";

    private final static String CREATE_FRIEND_TABLE = "create table if not exists"
            + " " + FriendDBField.TABLE_NAME
            + "(" + FriendDBField.ID + " " + "integer primary key autoincrement"
            + "," + FriendDBField.MY_ACCOUNT + " " + "varchar(200) not null"
            + "," + FriendDBField.FRIEND_ACCOUNT + " " + "varchar(200) null"
            + "," + FriendDBField.TIME + " " + "varchar(50) null" + ");";

    public SqliteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TEAM_TABLE);
        db.execSQL(CREATE_FRIEND_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
