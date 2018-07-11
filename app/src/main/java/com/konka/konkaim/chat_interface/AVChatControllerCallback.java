package com.konka.konkaim.chat_interface;

/**
 * Created by HP on 2018-5-22.
 */

public interface AVChatControllerCallback<T> {

    void onSuccess(T t);

    void onFailed(int code, String errorMsg);
}
