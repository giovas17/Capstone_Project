package com.softwaremobility.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by darkgeat on 3/16/16.
 */
public class MediaUtilities {

    public static final int MEDIA_TYPE_IMAGE = 100;
    public static final int REQUEST_CODE = 200;
    public static String mCurrentPhotoPath;

    public static File createImageFile(Activity context) throws IOException {
        // Create an image file name
        if(permissionsGranted(context)) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";

            File root = Environment.getExternalStorageDirectory();
            String packageName = context.getPackageName();
            File expPath = new File(root.toString() + "/Android/obb/" + packageName + "/MoninPictures/");
            if (!expPath.exists()){
                expPath.mkdirs();
            }

            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    expPath      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
            return image;
        }else {
            throw new IOException("No Permissions granted");
        }
    }

    public static void galleryAddPic(Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private static boolean permissionsGranted(Activity context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation
                if (ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},REQUEST_CODE);
                    return false;
                }else {
                    //No explanation needed, we can request the permissions.
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},REQUEST_CODE);
                    return false;
                }
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ){
                if (ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.CAMERA)){
                    ActivityCompat.requestPermissions(context,new String[]{ Manifest.permission.CAMERA},REQUEST_CODE);
                    return false;
                }else {
                    //No explanation needed, we can request the permissions.
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.CAMERA},REQUEST_CODE);
                    return false;
                }
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    public static void dispatchTakePictureIntent(Activity activity) {
        takePictureIntent(activity,null);
    }

    public static void dispatchTakePictureIntent(Fragment fragment){
        takePictureIntent(null,fragment);
    }

    private static void takePictureIntent(@Nullable Activity activity, @Nullable Fragment fragment){
        boolean isActivity = activity != null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(isActivity ? activity.getPackageManager() : fragment.getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = MediaUtilities.createImageFile(isActivity ? activity : fragment.getActivity());
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Foto", ex.getMessage().toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                if (isActivity){
                    activity.startActivityForResult(takePictureIntent, MEDIA_TYPE_IMAGE);
                }else {
                    fragment.startActivityForResult(takePictureIntent, MEDIA_TYPE_IMAGE);
                }
            }
        }
    }

    private static int getCameraPhotoOrientation(String imagePath){
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static Bitmap decodeSampledBitmapFromUri(String dir, int Width, int Height)
    {
        Bitmap rotatedBitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(dir, options);

            options.inSampleSize = calculateInSampleSize(options, Width, Height);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(dir, options);
            Matrix matrix = new Matrix();
            matrix.postRotate(getCameraPhotoOrientation(dir));
            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch(Exception e) {
            return null;
        }
        return rotatedBitmap;
    }

    private static int calculateInSampleSize( BitmapFactory.Options options, int Width, int Height)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int size_inicialize = 1;

        if (height > Height || width > Width)
        {
            if (width > height)
            {
                size_inicialize = Math.round((float)height / (float)Height);
            }
            else
            {
                size_inicialize = Math.round((float)width / (float)Width);
            }
        }
        return size_inicialize;
    }

    public static File getTempPicturePath(Context context){
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(),"SharingPictures");
        if (!directory.exists()){
            directory.mkdirs();
        }
        directory.setWritable(true);
        directory.setReadable(true);
        directory.setExecutable(true);
        File photo = new File(directory, "recipeSharing.jpg");
        photo.setWritable(true);

        if (photo.exists()) {
            photo.delete();
        }else{
            try {
                photo.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return photo;
    }

}
