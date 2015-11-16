package org.openhds.mobile.utilities;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by ben on 10/26/15.
 */
public class FileUtils {

    public static String relativeFilePath(String subDir, String fileName, boolean includeTimestamp) {
        String timestamp = includeTimestamp ? DateUtils.formatDateTimeIso(Calendar.getInstance()) + "-" : "";
        return subDir + File.separator + timestamp + fileName;
    }

    public static String openHdsExternalFilesPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + "org.openhds.mobile";
    }

    public static File writeAssetExternalFile(Context context, String assetName, String fileName) {
        File file = new File(openHdsExternalFilesPath() + File.separator + fileName);
        return writeAssetToFile(context, assetName, file);
    }

    public static File writeAssetTempFile(Context context, String assetName, String fileName) {
        File file = new File(context.getCacheDir() + File.separator + fileName);
        return writeAssetToFile(context, assetName, file);
    }

    public static File writeAssetToFile(Context context, String assetName, File file) {

        try {
            InputStream inputStream = context.getAssets().open(assetName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            file.getParentFile().mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(buffer);
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return file;
    }


}
