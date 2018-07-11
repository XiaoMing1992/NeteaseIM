package com.konka.konkaim.chat;

import android.os.Handler;

import com.konka.konkaim.util.LogUtil;
import com.netease.nimlib.sdk.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2018-5-22.
 */

public class AVChatTimeoutObserver {
    private static final String TAG = "AVChatTimeoutObserver";
    private List<TimeoutObserver> timeoutObservers = new ArrayList<>();
    private List<Observer<Integer>> timeoutObserverLocal = new ArrayList<>(1); // 来电or呼出超时监听
    private Handler uiHandler;
    private final int OUTGOING_TIME_OUT = 45 * 1000;
    private final int INCOMING_TIME_OUT = 55 * 1000;

    private static class InstanceHolder {
        public final static AVChatTimeoutObserver instance = new AVChatTimeoutObserver();
    }

    public static AVChatTimeoutObserver getInstance() {
        return InstanceHolder.instance;
    }

    private AVChatTimeoutObserver() {
        uiHandler = new Handler(AVChatKit.getContext().getMainLooper());
    }


    // 通知APP观察者
    private <T> void notifyObservers(List<Observer<T>> observers, T result) {
        if (observers == null || observers.isEmpty()) {
            return;
        }

        // 创建副本，为了使得回调到app后，app如果立即注销观察者，会造成List异常。
        List<Observer<T>> copy = new ArrayList<>(observers.size());
        copy.addAll(observers);

        for (Observer<T> o : copy) {
            o.onEvent(result);
        }
    }

    // 注册注销APP观察者
    private <T> void registerObservers(List<Observer<T>> observers, final Observer<T> observer, boolean register) {
        if (observers == null || observer == null) {
            return;
        }

        if (register) {
            observers.add(observer);
        } else {
            observers.remove(observer);
        }
    }

    public void observeTimeoutNotification(Observer<Integer> observer, boolean register, boolean isIncoming) {
        LogUtil.LogI(TAG, "observeTimeoutNotification->" + observer + "#" + register);
        registerObservers(timeoutObserverLocal, observer, register);
        if (register) {
            if (isIncoming) {
                addIncomingTimeout();
            } else {
                addOutgoingTimeout();
            }
        } else {
            removeAllTimeout();
        }
    }

    private class TimeoutObserver implements Runnable {

        TimeoutObserver() {

        }

        @Override
        public void run() {
            LogUtil.LogI(TAG, "notify timeout ");

            notifyObservers(timeoutObserverLocal, 0);
        }
    }


    private void addOutgoingTimeout() {

        TimeoutObserver timeoutObserver = new TimeoutObserver();
        timeoutObservers.add(timeoutObserver);

        uiHandler.postDelayed(timeoutObserver, OUTGOING_TIME_OUT);
    }


    private void removeAllTimeout() {

        LogUtil.LogI(TAG, "remove all timeout");

        for (TimeoutObserver observer : timeoutObservers) {
            uiHandler.removeCallbacks(observer);
        }
        timeoutObservers.clear();
    }

    private void addIncomingTimeout() {
        TimeoutObserver timeoutObserver = new TimeoutObserver();
        timeoutObservers.add(timeoutObserver);

        uiHandler.postDelayed(timeoutObserver, INCOMING_TIME_OUT);

    }

}

