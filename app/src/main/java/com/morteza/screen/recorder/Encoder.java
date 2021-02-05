package com.morteza.screen.recorder;

import java.io.IOException;
/**
 * @author Morteza
 * @version 2019/12/3
 */
interface Encoder {
    void prepare() throws IOException;

    void stop();

    void release();

    void setCallback(Callback callback);

    interface Callback {
        void onError(Encoder encoder, Exception exception);
    }
}
