package com.konka.konkaim.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.konka.konkaim.bean.DbBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2018-6-5.
 */

public class DBUtil {

    public static synchronized void  add(Context context, DbBean dbBean) {
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBField.MY_ACCOUNT, dbBean.getMy_account());
        values.put(DBField.FRIEND_ACCOUNT, dbBean.getFriend_account());
        values.put(DBField.FROM, dbBean.getChat_from());
        values.put(DBField.TO, dbBean.getChat_to());
        values.put(DBField.TIME, dbBean.getRecord_time());
        //values.put(DBField.TEAM_ID, dbBean.getTeamId());
        values.put(DBField.IS_CONNECT, dbBean.getIs_connect());
        //values.put(DBField.IS_TEAM, dbBean.getIs_team());
        values.put(DBField.IS_FRIEND, dbBean.getIs_friend());
        values.put(DBField.IS_OUT, dbBean.getIs_out());

        long id = db.insert(DBField.TABLE_NAME, null, values);
        System.out.println("DBUtil, add id="+id);
        db.close();
    }

    public static synchronized void delete(Context context, String myAccount, int id) {
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        String whereClause = DBField.MY_ACCOUNT + "=?"+" and "+DBField.ID + "=?";
        db.delete(DBField.TABLE_NAME, whereClause, new String[]{myAccount,String.valueOf(id)});
        db.close();
    }

/*    public static synchronized void delete(Context context, String myAccount, String teamId) {
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        String whereClause = DBField.MY_ACCOUNT + "=?"+" and "+DBField.TEAM_ID + "=?";
        db.delete(DBField.TABLE_NAME, whereClause, new String[]{myAccount,teamId});
        db.close();
    }*/

    public static synchronized void update(Context context, DbBean dbBean) {
        System.out.println("DBUtil, update DBField.ID="+dbBean.getId());

        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBField.ID, dbBean.getId());
        values.put(DBField.MY_ACCOUNT, dbBean.getMy_account());
        values.put(DBField.FRIEND_ACCOUNT, dbBean.getFriend_account());
        values.put(DBField.FROM, dbBean.getChat_from());
        values.put(DBField.TO, dbBean.getChat_to());
        values.put(DBField.TIME, dbBean.getRecord_time());
        //values.put(DBField.TEAM_ID, dbBean.getTeamId());
        values.put(DBField.IS_CONNECT, dbBean.getIs_connect());
        //values.put(DBField.IS_TEAM, dbBean.getIs_team());
        values.put(DBField.IS_FRIEND, dbBean.getIs_friend());
        values.put(DBField.IS_OUT, dbBean.getIs_out());

        String whereClause = DBField.ID + "=?";
        db.update(DBField.TABLE_NAME, values, whereClause, new String[]{String.valueOf(dbBean.getId())});
        db.close();
    }

/*    public static List<DbBean> query(Context context) {
        List<DbBean> dbBeanList = new ArrayList<>();
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        //String whereClause = DBField.ID + "=?";
        //String[] column = new String[]{DBField.ID, DBField.MY_ACCOUNT, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME, DBField.IS_CONNECT, DBField.IS_TEAM};
        String orderBy = DBField.TIME+" desc"; //降序排序
        Cursor cursor = db.query(DBField.TABLE_NAME, null, null, null, null, null, orderBy);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            DbBean dbBean = new DbBean();
            dbBean.setMy_account(cursor.getString(cursor.getColumnIndex(DBField.MY_ACCOUNT)));
            dbBean.setFriend_account(cursor.getString(cursor.getColumnIndex(DBField.FRIEND_ACCOUNT)));
            dbBean.setId(cursor.getInt(cursor.getColumnIndex(DBField.ID)));
            dbBean.setTeamId(cursor.getString(cursor.getColumnIndex(DBField.TEAM_ID)));
            dbBean.setChat_from(cursor.getString(cursor.getColumnIndex(DBField.FROM)));
            dbBean.setChat_to(cursor.getString(cursor.getColumnIndex(DBField.TO)));
            dbBean.setRecord_time(cursor.getString(cursor.getColumnIndex(DBField.TIME)));
            dbBean.setIs_connect(cursor.getInt(cursor.getColumnIndex(DBField.IS_CONNECT)));
            dbBean.setIs_team(cursor.getInt(cursor.getColumnIndex(DBField.IS_TEAM)));
            dbBean.setIs_friend(cursor.getInt(cursor.getColumnIndex(DBField.IS_FRIEND)));
            dbBean.setIs_out(cursor.getInt(cursor.getColumnIndex(DBField.IS_OUT)));

            dbBeanList.add(dbBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }*/

    public static List<DbBean> queryOnlyFriend(Context context, String myAccount) {
        List<DbBean> dbBeanList = new ArrayList<>();
        if (myAccount == null) return dbBeanList;

        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = DBField.MY_ACCOUNT + "=?";

        //String[] column = new String[]{DBField.ID, DBField.MY_ACCOUNT, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME, DBField.IS_CONNECT, DBField.IS_TEAM};
        String orderBy = DBField.TIME+" desc"; //降序排序
        Cursor cursor = db.query(DBField.TABLE_NAME, null, whereClause, new String[]{myAccount}, null, null, orderBy);
        //cursor.moveToFirst();
        while (cursor.moveToNext()) {
            DbBean dbBean = new DbBean();
            dbBean.setMy_account(cursor.getString(cursor.getColumnIndex(DBField.MY_ACCOUNT)));
            dbBean.setFriend_account(cursor.getString(cursor.getColumnIndex(DBField.FRIEND_ACCOUNT)));
            dbBean.setId(cursor.getInt(cursor.getColumnIndex(DBField.ID)));
            //dbBean.setTeamId(cursor.getString(cursor.getColumnIndex(DBField.TEAM_ID)));
            dbBean.setChat_from(cursor.getString(cursor.getColumnIndex(DBField.FROM)));
            dbBean.setChat_to(cursor.getString(cursor.getColumnIndex(DBField.TO)));
            dbBean.setRecord_time(cursor.getString(cursor.getColumnIndex(DBField.TIME)));
            dbBean.setIs_connect(cursor.getInt(cursor.getColumnIndex(DBField.IS_CONNECT)));
            //dbBean.setIs_team(cursor.getInt(cursor.getColumnIndex(DBField.IS_TEAM)));
            dbBean.setIs_friend(cursor.getInt(cursor.getColumnIndex(DBField.IS_FRIEND)));
            dbBean.setIs_out(cursor.getInt(cursor.getColumnIndex(DBField.IS_OUT)));

            dbBeanList.add(dbBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }

/*    public static List<DbBean> query(Context context, int id) {
        List<DbBean> dbBeanList = new ArrayList<>();
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = DBField.ID + "=?";
        //String[] column = new String[]{DBField.ID, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME};
        String orderBy = DBField.TIME+" desc"; //降序排序
        Cursor cursor = db.query(DBField.TABLE_NAME, null, whereClause, new String[]{String.valueOf(id)}, null, null, orderBy);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            DbBean dbBean = new DbBean();
            dbBean.setId(cursor.getInt(cursor.getColumnIndex(DBField.ID)));
            dbBean.setMy_account(cursor.getString(cursor.getColumnIndex(DBField.MY_ACCOUNT)));
            dbBean.setFriend_account(cursor.getString(cursor.getColumnIndex(DBField.FRIEND_ACCOUNT)));
            dbBean.setTeamId(cursor.getString(cursor.getColumnIndex(DBField.TEAM_ID)));
            dbBean.setChat_from(cursor.getString(cursor.getColumnIndex(DBField.FROM)));
            dbBean.setChat_to(cursor.getString(cursor.getColumnIndex(DBField.TO)));
            dbBean.setRecord_time(cursor.getString(cursor.getColumnIndex(DBField.TIME)));
            dbBean.setIs_connect(cursor.getInt(cursor.getColumnIndex(DBField.IS_CONNECT)));
            dbBean.setIs_team(cursor.getInt(cursor.getColumnIndex(DBField.IS_TEAM)));
            dbBean.setIs_friend(cursor.getInt(cursor.getColumnIndex(DBField.IS_FRIEND)));
            dbBean.setIs_out(cursor.getInt(cursor.getColumnIndex(DBField.IS_OUT)));

            dbBeanList.add(dbBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }*/

/*    public static List<DbBean> queryOnlyTeam(Context context, String myAccount) {
        List<DbBean> dbBeanList = new ArrayList<>();
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = DBField.MY_ACCOUNT + "=?"+" and "+DBField.IS_TEAM + "=?";

        //String[] column = new String[]{DBField.ID, DBField.MY_ACCOUNT, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME, DBField.IS_CONNECT, DBField.IS_TEAM};
        String orderBy = DBField.TIME+" desc"; //降序排序
        Cursor cursor = db.query(DBField.TABLE_NAME, null, whereClause, new String[]{myAccount,"1"}, null, null, orderBy);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            DbBean dbBean = new DbBean();
            dbBean.setMy_account(cursor.getString(cursor.getColumnIndex(DBField.MY_ACCOUNT)));
            dbBean.setFriend_account(cursor.getString(cursor.getColumnIndex(DBField.FRIEND_ACCOUNT)));
            dbBean.setId(cursor.getInt(cursor.getColumnIndex(DBField.ID)));
            dbBean.setTeamId(cursor.getString(cursor.getColumnIndex(DBField.TEAM_ID)));
            dbBean.setChat_from(cursor.getString(cursor.getColumnIndex(DBField.FROM)));
            dbBean.setChat_to(cursor.getString(cursor.getColumnIndex(DBField.TO)));
            dbBean.setRecord_time(cursor.getString(cursor.getColumnIndex(DBField.TIME)));
            dbBean.setIs_connect(cursor.getInt(cursor.getColumnIndex(DBField.IS_CONNECT)));
            dbBean.setIs_team(cursor.getInt(cursor.getColumnIndex(DBField.IS_TEAM)));
            dbBean.setIs_friend(cursor.getInt(cursor.getColumnIndex(DBField.IS_FRIEND)));
            dbBean.setIs_out(cursor.getInt(cursor.getColumnIndex(DBField.IS_OUT)));

            dbBeanList.add(dbBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }*/

/*    public static List<DbBean> queryByTeamId(Context context, String myaccount, String teamId) {
        List<DbBean> dbBeanList = new ArrayList<>();
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = DBField.MY_ACCOUNT + "=?"+" and "+DBField.TEAM_ID + "=?";
        //String[] column = new String[]{DBField.ID, DBField.MY_ACCOUNT, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME};
        String orderBy = DBField.TIME+" desc"; //降序排序
        Cursor cursor = db.query(DBField.TABLE_NAME, null, whereClause, new String[]{myaccount, teamId}, null, null, orderBy);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            DbBean dbBean = new DbBean();
            dbBean.setId(cursor.getInt(cursor.getColumnIndex(DBField.ID)));
            dbBean.setMy_account(cursor.getString(cursor.getColumnIndex(DBField.MY_ACCOUNT)));
            dbBean.setFriend_account(cursor.getString(cursor.getColumnIndex(DBField.FRIEND_ACCOUNT)));
            dbBean.setTeamId(cursor.getString(cursor.getColumnIndex(DBField.TEAM_ID)));
            dbBean.setChat_from(cursor.getString(cursor.getColumnIndex(DBField.FROM)));
            dbBean.setChat_to(cursor.getString(cursor.getColumnIndex(DBField.TO)));
            dbBean.setRecord_time(cursor.getString(cursor.getColumnIndex(DBField.TIME)));
            dbBean.setIs_connect(cursor.getInt(cursor.getColumnIndex(DBField.IS_CONNECT)));
            dbBean.setIs_team(cursor.getInt(cursor.getColumnIndex(DBField.IS_TEAM)));
            dbBean.setIs_friend(cursor.getInt(cursor.getColumnIndex(DBField.IS_FRIEND)));
            dbBean.setIs_out(cursor.getInt(cursor.getColumnIndex(DBField.IS_OUT)));
            System.out.println("database time is "+dbBean.getRecord_time());
            dbBeanList.add(dbBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }*/

    public static List<DbBean> queryForFriend(Context context, String myAccount, String friendAccount) {
        List<DbBean> dbBeanList = new ArrayList<>();
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = DBField.MY_ACCOUNT + "=?"+" and "+DBField.FRIEND_ACCOUNT + "=?";
        //String[] column = new String[]{DBField.ID, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME};
        String orderBy = DBField.TIME+" desc"; //降序排序
        Cursor cursor = db.query(DBField.TABLE_NAME, null, whereClause, new String[]{myAccount, friendAccount}, null, null, orderBy);

        //db.execSQL();
        //Cursor cursor = db.rawQuery("select * from "+DBField.TABLE_NAME+" where "+ DBField.MY_ACCOUNT+"="+myAccount+" and "+DBField.FRIEND_ACCOUNT + "="+friendAccount+"order by create desc", null);
        //cursor.moveToFirst();
        while (cursor.moveToNext()) {
            DbBean dbBean = new DbBean();
            dbBean.setId(cursor.getInt(cursor.getColumnIndex(DBField.ID)));
            dbBean.setMy_account(cursor.getString(cursor.getColumnIndex(DBField.MY_ACCOUNT)));
            dbBean.setFriend_account(cursor.getString(cursor.getColumnIndex(DBField.FRIEND_ACCOUNT)));
            //dbBean.setTeamId(cursor.getString(cursor.getColumnIndex(DBField.TEAM_ID)));
            dbBean.setChat_from(cursor.getString(cursor.getColumnIndex(DBField.FROM)));
            dbBean.setChat_to(cursor.getString(cursor.getColumnIndex(DBField.TO)));
            dbBean.setRecord_time(cursor.getString(cursor.getColumnIndex(DBField.TIME)));
            dbBean.setIs_connect(cursor.getInt(cursor.getColumnIndex(DBField.IS_CONNECT)));
            //dbBean.setIs_team(cursor.getInt(cursor.getColumnIndex(DBField.IS_TEAM)));
            dbBean.setIs_friend(cursor.getInt(cursor.getColumnIndex(DBField.IS_FRIEND)));
            dbBean.setIs_out(cursor.getInt(cursor.getColumnIndex(DBField.IS_OUT)));

            dbBeanList.add(dbBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }
}
