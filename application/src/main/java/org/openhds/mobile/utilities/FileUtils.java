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

    private File getExternalStorageFile(String subDir, String baseName, String extension, boolean appendTimestamp) {

        String timestamp = appendTimestamp ? "-" + DateUtils.formatDateTimeIso(Calendar.getInstance()) : "";
        String newName = baseName + timestamp + extension;

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String newPath = rootPath + File.separator
                + "Android" + File.separator
                + "data" + File.separator
                + "org.openhds.mobile" + File.separator
                + "files" + File.separator
                + subDir;

        File parentFolder = new File(newPath);
        if (!parentFolder.exists() && !parentFolder.mkdirs()) {
            return null;
        }

        return new File(newPath + File.separator + newName);
    }

    public static File writeAssetTempFile(Context context, String assetName, String fileName) {
        File file = new File(context.getCacheDir() + File.separator + fileName);
        if (!file.exists()) try {

            InputStream inputStream = context.getAssets().open(assetName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(buffer);
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return file;
    }


}
