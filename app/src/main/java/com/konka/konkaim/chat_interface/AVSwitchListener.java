package com.konka.konkaim.chat_interface;

/**
 * 音视频切换接口
 */

public interface AVSwitchListener {
    /**
     * 视频切换为音频
     */
    void onVideoToAudio();

    /**
     * 音频切换为视频
     */
    void onAudioToVideo();

    /**
     * 同意将音频切换为视频
     */
    void onReceiveAudioToVideoAgree();
}
