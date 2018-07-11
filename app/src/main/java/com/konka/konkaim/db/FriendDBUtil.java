package com.konka.konkaim.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.konka.konkaim.bean.FriendBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2018-6-5.
 */

public class FriendDBUtil {

    public static synchronized void  add(Context context, FriendBean friendBean) {
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FriendDBField.MY_ACCOUNT, friendBean.getMy_account());
        values.put(FriendDBField.FRIEND_ACCOUNT, friendBean.getFriend_account());
        values.put(FriendDBField.TIME, friendBean.getRecord_time());

        long id = db.insert(FriendDBField.TABLE_NAME, null, values);
        System.out.println("FriendDBUtil, add id="+id);
        db.close();
    }

    public static synchronized void delete(Context context, String myAccount, int id) {
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        String whereClause = FriendDBField.MY_ACCOUNT + "=?"+" and "+FriendDBField.ID + "=?";
        db.delete(FriendDBField.TABLE_NAME, whereClause, new String[]{myAccount,String.valueOf(id)});
        db.close();
    }

    public static List<FriendBean> queryOnlyFriend(Context context, String myAccount) {
        List<FriendBean> dbBeanList = new ArrayList<>();
        if (myAccount == null) return dbBeanList;

        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = FriendDBField.MY_ACCOUNT + "=?";

        //String[] column = new String[]{DBField.ID, DBField.MY_ACCOUNT, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME, DBField.IS_CONNECT, DBField.IS_TEAM};
        String orderBy = FriendDBField.TIME+" desc"; //降序排序
        Cursor cursor = db.query(FriendDBField.TABLE_NAME, null, whereClause, new String[]{myAccount}, null, null, orderBy);
        //cursor.moveToFirst();
        while (cursor.moveToNext()) {
            FriendBean friendBean = new FriendBean();
            friendBean.setMy_account(cursor.getString(cursor.getColumnIndex(FriendDBField.MY_ACCOUNT)));
            friendBean.setFriend_account(cursor.getString(cursor.getColumnIndex(FriendDBField.FRIEND_ACCOUNT)));
            friendBean.setId(cursor.getInt(cursor.getColumnIndex(FriendDBField.ID)));
            friendBean.setRecord_time(cursor.getString(cursor.getColumnIndex(FriendDBField.TIME)));
            dbBeanList.add(friendBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }


    public static List<FriendBean> queryOnlyFriend(Context context, String myAccount, String friendAccount) {
        List<FriendBean> dbBeanList = new ArrayList<>();
        if (myAccount == null) return dbBeanList;

        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = FriendDBField.MY_ACCOUNT + "=?" +" and "+FriendDBField.FRIEND_ACCOUNT + "=?";

        //String[] column = new String[]{DBField.ID, DBField.MY_ACCOUNT, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME, DBField.IS_CONNECT, DBField.IS_TEAM};
        String orderBy = FriendDBField.TIME+" desc"; //降序排序
        Cursor cursor = db.query(FriendDBField.TABLE_NAME, null, whereClause, new String[]{myAccount, friendAccount}, null, null, orderBy);
        //cursor.moveToFirst();
        while (cursor.moveToNext()) {
            FriendBean friendBean = new FriendBean();
            friendBean.setMy_account(cursor.getString(cursor.getColumnIndex(FriendDBField.MY_ACCOUNT)));
            friendBean.setFriend_account(cursor.getString(cursor.getColumnIndex(FriendDBField.FRIEND_ACCOUNT)));
            friendBean.setId(cursor.getInt(cursor.getColumnIndex(FriendDBField.ID)));
            friendBean.setRecord_time(cursor.getString(cursor.getColumnIndex(FriendDBField.TIME)));
            dbBeanList.add(friendBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }
}
