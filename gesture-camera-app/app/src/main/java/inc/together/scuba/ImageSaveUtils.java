package inc.together.scuba;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by stefan on 28.11.2017.
 */

public class ImageSaveUtils {

    public static void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root, "Pictures");
        myDir.mkdirs();

        String fName = "scuba-diver-" + System.currentTimeMillis() + ".jpg";
        File file = new File(myDir, fName);

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

}
