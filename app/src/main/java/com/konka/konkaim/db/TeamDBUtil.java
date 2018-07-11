package com.konka.konkaim.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.konka.konkaim.bean.TeamDbBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2018-6-5.
 */

public class TeamDBUtil {

    public static synchronized void add(Context context, TeamDbBean teamDbBean) {
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TeamDBField.MY_ACCOUNT, teamDbBean.getMy_account());
        values.put(TeamDBField.TIME, teamDbBean.getRecord_time());
        values.put(TeamDBField.TEAM_ID, teamDbBean.getTeamId());
        values.put(TeamDBField.TEAM_NAME, teamDbBean.getTeam_name());

        long id = db.insert(TeamDBField.TABLE_NAME, null, values);
        System.out.println("TeamDBUtil, add id=" + id);
        db.close();
    }

    public static synchronized void delete(Context context, String myAccount, String teamId) {
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        String whereClause = TeamDBField.MY_ACCOUNT + "=?" + " and " + TeamDBField.TEAM_ID + "=?";
        db.delete(TeamDBField.TABLE_NAME, whereClause, new String[]{myAccount, teamId});
        db.close();
    }

    public static synchronized void update(Context context, TeamDbBean teamDbBean) {
        System.out.println("TeamDBUtil, update-->id=" + teamDbBean.getId()+", team_name="+teamDbBean.getTeam_name());
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TeamDBField.ID, teamDbBean.getId());
        values.put(TeamDBField.MY_ACCOUNT, teamDbBean.getMy_account());
        values.put(TeamDBField.TIME, teamDbBean.getRecord_time());
        values.put(TeamDBField.TEAM_ID, teamDbBean.getTeamId());
        values.put(TeamDBField.TEAM_NAME, teamDbBean.getTeam_name());

        String whereClause = TeamDBField.ID + "=?";
        db.update(TeamDBField.TABLE_NAME, values, whereClause, new String[]{String.valueOf(teamDbBean.getId())});
        db.close();
    }


    public static List<TeamDbBean> queryOnlyTeam(Context context, String myAccount) {

        List<TeamDbBean> dbBeanList = new ArrayList<>();
        if (myAccount == null) return dbBeanList;

        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = TeamDBField.MY_ACCOUNT + "=?";

        //String[] column = new String[]{DBField.ID, DBField.MY_ACCOUNT, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME, DBField.IS_CONNECT, DBField.IS_TEAM};
        String orderBy = TeamDBField.TIME + " desc"; //降序排序
        Cursor cursor = db.query(TeamDBField.TABLE_NAME, null, whereClause, new String[]{myAccount}, null, null, orderBy);
        //cursor.moveToFirst();
        while (cursor.moveToNext()) {
            TeamDbBean teamDbBean = new TeamDbBean();
            teamDbBean.setMy_account(cursor.getString(cursor.getColumnIndex(TeamDBField.MY_ACCOUNT)));
            teamDbBean.setId(cursor.getInt(cursor.getColumnIndex(TeamDBField.ID)));
            teamDbBean.setTeamId(cursor.getString(cursor.getColumnIndex(TeamDBField.TEAM_ID)));
            teamDbBean.setRecord_time(cursor.getString(cursor.getColumnIndex(TeamDBField.TIME)));
            teamDbBean.setTeam_name(cursor.getString(cursor.getColumnIndex(TeamDBField.TEAM_NAME)));

            dbBeanList.add(teamDbBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }

    public static List<TeamDbBean> queryByTeamId(Context context, String myaccount, String teamId) {
        List<TeamDbBean> dbBeanList = new ArrayList<>();
        SqliteHelper sqliteHelper = new SqliteHelper(context);
        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        String whereClause = TeamDBField.MY_ACCOUNT + "=?" + " and " + TeamDBField.TEAM_ID + "=?";
        //String[] column = new String[]{DBField.ID, DBField.MY_ACCOUNT, DBField.TEAM_ID, DBField.FROM, DBField.TO, DBField.TIME};
        String orderBy = DBField.TIME + " desc"; //降序排序
        Cursor cursor = db.query(TeamDBField.TABLE_NAME, null, whereClause, new String[]{myaccount, teamId}, null, null, orderBy);
        //cursor.moveToFirst();
        while (cursor.moveToNext()) {
            TeamDbBean teamDbBean = new TeamDbBean();
            teamDbBean.setId(cursor.getInt(cursor.getColumnIndex(TeamDBField.ID)));
            teamDbBean.setMy_account(cursor.getString(cursor.getColumnIndex(TeamDBField.MY_ACCOUNT)));
            teamDbBean.setTeamId(cursor.getString(cursor.getColumnIndex(TeamDBField.TEAM_ID)));
            teamDbBean.setRecord_time(cursor.getString(cursor.getColumnIndex(TeamDBField.TIME)));
            teamDbBean.setTeam_name(cursor.getString(cursor.getColumnIndex(TeamDBField.TEAM_NAME)));

            System.out.println("database time is " + teamDbBean.getRecord_time());
            dbBeanList.add(teamDbBean);
        }
        cursor.close();
        db.close();
        return dbBeanList;
    }

}
