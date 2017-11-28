package inc.together.scuba;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by stefan on 28.11.2017.
 */

public class ImageSaveUtils {

    private static final String IMAGE_DIRECTORY = "Pictures/ScubaDiver/";

    public static void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File targetDirectory = new File(root, IMAGE_DIRECTORY);
        targetDirectory.mkdirs();

        String fName = "scuba-diver-" + System.currentTimeMillis() + ".jpg";
        File file = new File(targetDirectory, fName);

        if (file.exists()) {
            file.delete();
        }

        Log.i("LOAD", root + fName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void notifyGallery(Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        File targetDirectory = new File(root, IMAGE_DIRECTORY);

        MediaScannerConnection.scanFile(context,
                new String[]{targetDirectory.getAbsolutePath()}, null,
                null);
    }

}
