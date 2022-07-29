package com.xpfriend.tydrone.factory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.unity3d.player.UnityPlayer;
import com.xpfriend.tydrone.core.OutputStreamFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

class AndroidOutputStreamFactory extends OutputStreamFactory {
    @Override
    public OutputStream createOutputStream(String ext, String mimeType) throws IOException {
        String name = OutputStreamFactory.getName();

        ContentValues cvs = new ContentValues();
        cvs.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        cvs.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        cvs.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        //cvs.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        //cvs.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);

        ContentResolver resolver = UnityPlayer.currentActivity.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), cvs);

        return resolver.openOutputStream(uri);
    }
    
    @Override
    public File createTempFile(String ext) throws IOException {
        File dir = UnityPlayer.currentActivity.getApplication().getCacheDir();
        File file = File.createTempFile("tydrone-", ext, dir);
        file.deleteOnExit();
        return file;
    }
}
