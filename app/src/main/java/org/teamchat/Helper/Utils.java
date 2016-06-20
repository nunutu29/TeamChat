package org.teamchat.Helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.teamchat.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Johnny on 20/04/2016.
 * Questa classe conterra tutti i metodi utili da utilizzare in piu posizioni
 */
public class Utils {

    public static boolean hasInternet(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(context, context.getResources().getString(R.string.hint_no_internet),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static String saveToInternalStorage(Context context, Bitmap bitmap, String ToDir, String fileName){
        if(bitmap == null) return null;
        File dir = context.getDir(ToDir, Context.MODE_PRIVATE);
        File mypath = new File(dir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir.getAbsolutePath();
    }

    public static Bitmap loadFileFromStorage(Context context, String path, String file){
        Bitmap b = null;
        File dir = context.getDir(path, Context.MODE_PRIVATE);
        File f=new File(dir, file);
        if(f.exists()) {
            try {
                b = BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    public static boolean deleteFileFromStorage(Context context, String path, String file) {
        Bitmap b = null;
        File dir = context.getDir(path, Context.MODE_PRIVATE);
        File f = new File(dir, file);
        return f.exists() && f.delete();
    }

    public static String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        Date currentLocalTime = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(currentLocalTime);
    }

    public static String getTimeStamp(String dateStr) {
        if(dateStr == null) return null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";
        try {
            Date date = format.parse(dateStr);
            format = new SimpleDateFormat("HH:mm");
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public static String getDateTimeStamp(String dateStr) {
        if(dateStr == null) return null;
        Calendar calendar = Calendar.getInstance();
        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("HH:mm") : new SimpleDateFormat("dd LLL, HH:mm");
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if(activity.getCurrentFocus() == null) return;
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void showSoftKeyboard(Activity activity, View view) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, 0);
    }

    public static boolean isSoftKeyboardShowing(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        return inputMethodManager.isActive();
    }

    public static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static Intent OpenGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return intent;
    }

    public static Intent PerformCropIntent(Uri picUri){
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            return cropIntent;
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            return null;
        }
    }
}
