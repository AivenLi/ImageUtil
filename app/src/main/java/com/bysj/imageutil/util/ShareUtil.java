package com.bysj.imageutil.util;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bysj.imageutil.R;

import java.io.File;
import java.util.ArrayList;

/**
 * 分享工具类
 *
 * Create on 2021-3-10
 */

public class ShareUtil {

    private static final String TAG = "shareUtil";

    /**
     * 分享本APP产生的所有图片到某个app
     * @param context
     */
    public static void share(Context context) {

        Intent intent = new Intent();
        ArrayList<Uri> uris = new ArrayList<>();
        File[] files = new File("storage/emulated/0/IEnhance").listFiles();
        if ( files != null ) {
            /**
             * Android 7.0以上
             */
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {

                for ( File file : files ) {

                    Uri uri = getImageContentUri(context, file);
                    uris.add(uri);
                }
            } else {

                for ( File file : files ) {

                    uris.add(Uri.fromFile(file));
                }
            }
            if ( uris.size() > 0 ) {

                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_title)));
            } else {

                Toast.makeText(context, "图片为空", Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(context, "图库暂无图片", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 分享一张图片到其他的APP
     * @param context
     * @param filePath
     */
    public static void share(Context context, String filePath) {

        ArrayList<String> fs = new ArrayList<>();
        fs.add(filePath);
        share(context, fs);
    }

    public static void share(Context context, ArrayList<String> files) {

        Intent intent = new Intent();
        ArrayList<Uri> uris = new ArrayList<>();
        if ( files != null && files.size() > 0 ) {
            /**
             * Android 7.0以上
             */
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {

                for ( String fs : files ) {

                    File file = new File(fs);
                    if ( file.exists() ) {

                        Uri uri = getImageContentUri(context, file);
                        uris.add(uri);
                    }
                }
            } else {

                for ( String fs : files ) {

                    File file = new File(fs);
                    if ( file.exists() ) {

                        uris.add(Uri.fromFile(file));
                    }
                }
            }
            if ( uris.size() > 0 ) {

                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_title)));
            } else {

                Toast.makeText(context, "图片为空", Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(context, "图片为空", Toast.LENGTH_SHORT).show();
        }
    }

    private static Uri getImageContentUri(Context context, File imageFile) {

        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if ( cursor != null && cursor.moveToFirst() ) {

            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {

            if ( imageFile.exists() ) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else return null;
        }
    }
}
