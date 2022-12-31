package com.xpfriend.tydrone.factory;

import com.unity3d.player.UnityPlayer;
import com.xpfriend.tydrone.core.Info;
import com.xpfriend.tydrone.core.Logger;
import com.xpfriend.tydrone.core.OutputStreamFactory;
import com.xpfriend.tydrone.core.Startable;
import com.xpfriend.tydrone.recorder.FFmpegVideoRecorder;
import com.xpfriend.tydrone.sensor.DepthSensor;
import com.xpfriend.tydrone.talker.Talker;
import com.xpfriend.tydrone.telloio.ChannelReceiver;
import com.xpfriend.tydrone.telloio.ChannelRequester;
import com.xpfriend.tydrone.telloio.FFmpegVideoReceiver;
import com.xpfriend.tydrone.telloio.MessageHandlerManager;
import com.xpfriend.tydrone.telloio.TimerJobScheduler;
import com.xpfriend.tydrone.telloio.VideoForwarder;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

public class AndroidStartableFactory {
    private static final AndroidStartableFactory instance = new AndroidStartableFactory(new AndroidLogger(), new AndroidOutputStreamFactory());

    private final Logger logger;
    private final OutputStreamFactory outputStreamFactory;

    private AndroidStartableFactory(Logger logger, OutputStreamFactory outputStreamFactory) {
        this.logger = logger;
        this.outputStreamFactory = outputStreamFactory;
    }

    public static AndroidStartableFactory getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public List<Startable> createStartables(Info info) throws IOException {
        UnityPlayer.currentActivity.getApplication().registerActivityLifecycleCallbacks(new AutoStop(info));

        MessageHandlerManager handlerManager = new MessageHandlerManager(outputStreamFactory, logger);
        DatagramChannel channel = handlerManager.connect();
        List<Startable> startables = new ArrayList<>();
        startables.add(new ChannelReceiver(handlerManager));
        startables.add(new ChannelRequester(handlerManager));
        startables.add(new TimerJobScheduler(handlerManager));
        startables.add(new VideoForwarder(channel));
        startables.add(new FFmpegVideoReceiver());
        startables.add(new Talker());
        startables.add(new FFmpegVideoRecorder(outputStreamFactory));
        startables.add(new DepthSensor());
        return startables;
    }
}
