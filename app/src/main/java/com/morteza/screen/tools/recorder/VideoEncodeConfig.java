package com.morteza.screen.tools.recorder;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.morteza.screen.tools.Utils;

import org.jetbrains.annotations.NotNull;

/**
 * @author Morteza
 * @version 2019/12/3
 */
public class VideoEncodeConfig {
    public final int width;
    public final int height;
    public final int bitrate;
    public final int frameRate;
    public final int iFrameInterval;
    public final String codecName;
    public final String mimeType;
    public final int dpi;
    public final MediaCodecInfo.CodecProfileLevel codecProfileLevel;

    /**
     * @param codecName         selected codec name, maybe null
     * @param mimeType          video MIME type, cannot be null
     * @param codecProfileLevel profile level for video encoder nullable
     */
    public VideoEncodeConfig(int width, int height, int bitrate, int dpi,
                             int frameRate, int iFrameInterval,
                             String codecName, String mimeType,
                             MediaCodecInfo.CodecProfileLevel codecProfileLevel) {
        this.dpi = dpi;
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.frameRate = frameRate;
        this.iFrameInterval = iFrameInterval;
        this.codecName = codecName;
        this.mimeType = (mimeType);
        this.codecProfileLevel = codecProfileLevel;
    }

    MediaFormat toFormat() {
        MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
        if (codecProfileLevel != null && codecProfileLevel.profile != 0 && codecProfileLevel.level != 0) {
            format.setInteger(MediaFormat.KEY_PROFILE, codecProfileLevel.profile);
            format.setInteger("level", codecProfileLevel.level);
        }
        // maybe useful
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 10_000_000);
        return format;
    }

    @NotNull
    @Override
    public String toString() {
        return "VideoEncodeConfig{" +
                "width=" + width +
                ", height=" + height +
                ", bitrate=" + bitrate +
                ", frameRate=" + frameRate +
                ", iFrameInterval=" + iFrameInterval +
                ", codecName='" + codecName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", codecProfileLevel=" + (codecProfileLevel == null ? "" : Utils.avcProfileLevelToString(codecProfileLevel)) +
                '}';
    }
}