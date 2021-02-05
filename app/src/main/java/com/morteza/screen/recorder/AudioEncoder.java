package com.morteza.screen.recorder;

import android.media.MediaFormat;

/**
 * @author Morteza
 * @version 2019/12/3
 */
class AudioEncoder extends BaseEncoder {
    private final AudioEncodeConfig mConfig;

    AudioEncoder(AudioEncodeConfig config) {
        super(config.codecName);
        this.mConfig = config;
    }

    @Override
    protected MediaFormat createMediaFormat() {
        return mConfig.toFormat();
    }

}
