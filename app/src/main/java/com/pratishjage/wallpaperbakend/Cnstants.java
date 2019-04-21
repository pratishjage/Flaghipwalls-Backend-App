package com.pratishjage.wallpaperbakend;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class Cnstants {

public static String IMAGES_ARRAY="imgs";

    public static File compressImage(Uri uri, Context context) {
        File actualImage = null, compressedImageFile = null;
        try {
            actualImage = FileUtil.from(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            compressedImageFile = new Compressor(context).compressToFile(actualImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (actualImage != null && compressedImageFile != null) {
            return compressedImageFile;
        } else {
            return null;
        }
    }
}
