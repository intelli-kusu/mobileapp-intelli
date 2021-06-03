package com.intellicare.utils;

import android.content.Context;

import com.intellicare.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MockDataHelper {
    private final String PAST_VISIT_LIST = "pastvisitmock.json";

    public static String getPastVisits(Context c) {
        InputStream is = c.getResources().openRawResource(R.raw.pastvisitmock);
        return readTextFile(is);
    }
    public static String getVisitInfo(Context c) {
        InputStream is = c.getResources().openRawResource(R.raw.visitinfomock);
        return readTextFile(is);
    }

    private static String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toString();
    }
}
