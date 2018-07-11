package com.konka.konkaim.api;

import com.konka.konkaim.bean.BaseBean;

/**
 * Created by HP on 2018-5-8.
 */

public interface HttpListener<T> {
    void fail(Throwable e, String type);
    void success(T bean, String type);
}
