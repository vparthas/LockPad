package com.varunp.lockpad.utils.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Varun on 7/27/2016.
 */
public class ImageUtils
{
//    public static String BitMapToString(Bitmap bitmap)
//    {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] b = baos.toByteArray();
//        String temp = Base64.encodeToString(b, Base64.DEFAULT);
//        return temp;
//    }
//
//    public static Bitmap StringToBitMap(String encodedString)
//    {
//        try
//        {
//            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
//            return bitmap;
//        }
//        catch(Exception e)
//        {
//            e.getMessage();
//            return null;
//        }
//    }

    public static String encodeToBase64(Bitmap image)
    {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap getBitmapFromUri(Uri uri, Context context)
    {
        Bitmap bitmap;
        try
        {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Error loading image.", Toast.LENGTH_SHORT).show();
            return null;
        }

        if(bitmap == null)
        {
            Log.d("Image", "null bitmap");
        }
        return bitmap;
    }

    public static boolean writeToFile(String filename, Bitmap bmp)
    {
        FileOutputStream out = null;
        boolean ret = false;
        try
        {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            ret = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return ret;
        }
    }

    public static byte[] getfileBytes(Uri uri, Context context)
    {
        byte[] ret = null;

        try
        {
            InputStream iStream =  context.getContentResolver().openInputStream(uri);
            ret = getBytes(iStream);
        }
        catch (Exception e)
        {
            Log.d("Image", e.getMessage());
        }

        return ret;
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static final int MAX_SIZE = 100000;
    /**
     * reduces the size of the image
     * @param image
     * @param maxSize
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}
