package com.xpfriend.tydrone.talker;

import android.media.AudioAttributes;
import android.media.SoundPool;

import com.unity3d.player.UnityPlayer;
import com.xpfriend.tydrone.R;
import com.xpfriend.tydrone.core.Info;
import com.xpfriend.tydrone.core.Startable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Talker extends Startable {

    @Override
    public void start(Info info) throws Exception {
        super.start(info);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        SoundPool soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(3)
                .build();

        Map<String, Integer> soundMap = createSoundMap(soundPool);
        soundPool.setOnLoadCompleteListener((SoundPool pool, int sampleId, int status) -> logi("sound loaded: " + sampleId));

        String lastCommand = "";
        String lastNotice = "";
        while (info.isActive()) {
            sleep(10);
            String command = translate(info.getSentCommand(), lastCommand);
            String notice = translate(info.getNotice(), lastNotice);

            if (!notice.isEmpty() && !Objects.equals(lastNotice, notice)) {
                play(soundMap, soundPool, notice);
                lastNotice = notice;
            }
            if (!command.isEmpty() && !Objects.equals(lastCommand, command)) {
                play(soundMap, soundPool, command);
                lastCommand = command;
            }
        }
        logi("done");
    }

    private String translate(String message, String defaultMessage) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        if (!message.startsWith("stick")) {
            return message.split(" ")[0].toLowerCase();
        }

        try {
            String[] s = message.split(" ");
            StickCommand rx = new StickCommand(s[1], "left", "right", defaultMessage);
            StickCommand ry = new StickCommand(s[2], "backward", "forward", defaultMessage);
            StickCommand lx = new StickCommand(s[3], "ccw", "cw", defaultMessage);
            StickCommand ly = new StickCommand(s[4], "down", "up", defaultMessage);

            StickCommand value = rx.select(ry.select(ly));
            if (!value.isZero()) {
                return value.toString();
            }

            if (!lx.isZero()) {
                return lx.toString();
            }
        } catch (Exception e) {
            loge(e);
        }

        return "";
    }

    private void play(Map<String, Integer> soundMap, SoundPool soundPool, String key) {
        Integer sound = soundMap.get(key);
        if (sound == null) {
            return;
        }
        soundPool.play(sound, 1f, 1f, 0, 0, 1f);
    }

    private Map<String, Integer> createSoundMap(SoundPool soundPool) {
        Map<String, Integer> map = new HashMap<>();
        map.put("back", soundPool.load(UnityPlayer.currentActivity, R.raw.back, 1));
        map.put("ccw", soundPool.load(UnityPlayer.currentActivity, R.raw.ccw, 1));
        map.put("cw", soundPool.load(UnityPlayer.currentActivity, R.raw.cw, 1));
        map.put("down", soundPool.load(UnityPlayer.currentActivity, R.raw.down, 1));
        map.put("error", soundPool.load(UnityPlayer.currentActivity, R.raw.error, 1));
        map.put("forward", soundPool.load(UnityPlayer.currentActivity, R.raw.forward, 1));
        map.put("left", soundPool.load(UnityPlayer.currentActivity, R.raw.left, 1));
        map.put("ok", soundPool.load(UnityPlayer.currentActivity, R.raw.ok, 1));
        map.put("right", soundPool.load(UnityPlayer.currentActivity, R.raw.right, 1));
        map.put("up", soundPool.load(UnityPlayer.currentActivity, R.raw.up, 1));
        map.put("land", soundPool.load(UnityPlayer.currentActivity, R.raw.land, 1));
        map.put("takeoff", soundPool.load(UnityPlayer.currentActivity, R.raw.takeoff, 1));
        map.put("picture", soundPool.load(UnityPlayer.currentActivity, R.raw.picture, 1));
        return map;
    }

    private class StickCommand {
        private final String name;
        private final float value;
        private final boolean isDefault;

        public StickCommand(String textValue, String a, String b, String c) {
            float value = Float.parseFloat(textValue);
            this.name = value < 0 ? a : b;
            this.value = Math.abs(value);
            this.isDefault = Objects.equals(name, c);
        }

        public StickCommand select(StickCommand command) {
            if (isDefault) {
                return value + 0.05f > command.value ? this : command;
            }
            return this.value > command.value ? this : command;
        }

        @Override
        public String toString() {
            return name;
        }

        public boolean isZero() {
            return this.value < 0.01f;
        }
    }
}
