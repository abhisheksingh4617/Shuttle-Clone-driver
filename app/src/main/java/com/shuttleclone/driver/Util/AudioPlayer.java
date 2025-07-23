package com.shuttleclone.driver.Util;


import static com.shuttleclone.driver.Util.UtilMethodsKt.myLog;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.io.IOException;


public class AudioPlayer {
    public static MediaPlayer mediaPlayer;
    public static boolean isplayingAudio = false;
    public static long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};


    public static void playAudio(Context c,int sound) {
        try {
            Uri music = Uri.parse("android.resource://" +c.getPackageName()  + "/" + sound);
//            Uri music = Uri.parse("android.resource://" +c.getPackageName()  + "/" + R.raw.booking_sound);

            AudioManager audioManager = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume,  AudioManager.FLAG_PLAY_SOUND);


            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(c, music);
            if (!mediaPlayer.isPlaying()) {
                isplayingAudio = true;
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();

//                savePref(c, AppConstants.IS_PLAYING_AUDIO,true);
            }

            Vibrator vibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern,-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            myLog("AudioPlayer", "playAudio: Eee=" + e.getLocalizedMessage());
        }
    }

    public static void playAudioOnce(Context c,int sound) {
        try {
            Uri music = Uri.parse("android.resource://" +c.getPackageName()  + "/" + sound);

            AudioManager audioManager = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume,  AudioManager.FLAG_PLAY_SOUND);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(c, music);
            if (!mediaPlayer.isPlaying()) {
                isplayingAudio = true;
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mediaPlayer.setLooping(false);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }

            Vibrator vibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern,-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            myLog("AudioPlayer", "playAudio: Eee=" + e.getLocalizedMessage());
        }
    }

    public static void stopAudio() {
        try {
            isplayingAudio = false;
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        } catch (Exception e) {
            myLog("AudioPlayer", "stopAudio: Eee=" + e.getLocalizedMessage());
        }


    }
}
