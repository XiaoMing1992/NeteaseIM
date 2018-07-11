package com.konka.konkaim.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2018-6-14.
 */

public class ActivityHelper {
    public static volatile ActivityHelper instance;
    private List<Activity> activities;
    private ActivityHelper(){
        activities = new ArrayList<>();
    }

    public static ActivityHelper getInstance(){
        if (instance == null){
            synchronized (ActivityHelper.class){
                if (instance == null){
                    instance = new ActivityHelper();
                }
            }
        }
        return instance;
    }

    public void addActivity(Activity activity){
        activities.add(activity);
    }

    public void finishActivity(){
        for (int i=0;i<activities.size();i++){
            activities.get(i).finish();
        }
    }
}
