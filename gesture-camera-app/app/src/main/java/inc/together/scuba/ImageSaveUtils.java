package inc.together.scuba;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * Created by stefan on 28.11.2017.
 */

public class ImageSaveUtils {

    private static final String IMAGE_DIRECTORY = "Pictures/ScubaDiver/";

    static void saveImage(Context context, Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File targetDirectory = new File(root, IMAGE_DIRECTORY);
        targetDirectory.mkdirs();

        String fName = "scuba-diver-" + System.currentTimeMillis() + ".jpg";
        File takenFile = new File(targetDirectory, fName);

        if (takenFile.exists()) {
            takenFile.delete();
        }

        Log.i("LOAD", targetDirectory.getAbsolutePath() + "/" + fName);
        try {
            FileOutputStream out = new FileOutputStream(takenFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            // Meta Data otherwise image won't show up
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fName);
            values.put(MediaStore.Images.Media.DESCRIPTION, fName + " is taken from scuba divemaster app");
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.ImageColumns.BUCKET_ID, takenFile.toString().toLowerCase(Locale.GERMAN).hashCode());
            values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, takenFile.toString().toLowerCase(Locale.GERMAN).hashCode());
            values.put("_data", takenFile.getAbsolutePath());

            ContentResolver cr = context.getContentResolver();
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
