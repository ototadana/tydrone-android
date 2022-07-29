package com.xpfriend.tydrone;

import com.xpfriend.tydrone.core.Facade;
import com.xpfriend.tydrone.core.Info;
import com.xpfriend.tydrone.core.Runner;
import com.xpfriend.tydrone.factory.AndroidStartableFactory;

import java.io.IOException;

public class AndroidMain extends Facade {
    @Override
    protected void handleRun(Info info) throws IOException {
        AndroidStartableFactory factory = AndroidStartableFactory.getInstance();
        new Runner(factory.getLogger()).run(info, factory.createStartables(info));
    }
}
