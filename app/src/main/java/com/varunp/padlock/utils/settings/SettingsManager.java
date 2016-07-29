package com.varunp.padlock.utils.settings;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.varunp.padlock.utils.file.FileManager;

import org.json.JSONObject;

/**
 * Created by Varun on 7/29/2016.
 */
public class SettingsManager
{
    public static final String SETTINGS_FILE_NAME = "settings.json";

    public static final String CLOSE_ON_PAUSE = "pause-on=close";

    public static boolean createFile(Context context)
    {
        try
        {
            JSONObject defaults = new JSONObject();
            defaults.put(CLOSE_ON_PAUSE, true);

            data = defaults;

            FileManager fileManager = new FileManager(context);
            return fileManager.saveFile(true, SETTINGS_FILE_NAME, data.toString());
        }
        catch (Exception e)
        {
            Log.d("Settings", e.getMessage());
            return false;
        }
    }

    public static boolean putBoolean(String key, boolean val, Context context)
    {
        try
        {
            if(data.has(key))
                data.remove(key);
            data.put(key, val);
            return commit(context);
        }
        catch (Exception e)
        {
            Log.d("Settings", e.getMessage());
            return false;
        }
    }

    private static boolean commit(Context context)
    {
        init(context);

        FileManager fileManager = new FileManager(context);
        return fileManager.saveFile(true, SETTINGS_FILE_NAME, data.toString());
    }

    private static JSONObject data;

    public static void init(Context context)
    {
        FileManager fileManager = new FileManager(context);
        if(!fileManager.exists(true, false, SETTINGS_FILE_NAME))
            createFile(context);

        if(data == null)
        {
            try
            {
                String raw = fileManager.readFile(true, SETTINGS_FILE_NAME);
                data = new JSONObject(raw);
            }
            catch (Exception e)
            {
                Toast.makeText(context, "Error loading preferences. Using defaults instead.", Toast.LENGTH_SHORT).show();
                createFile(context);
            }
        }
    }

    public static boolean getBoolean(String key, boolean def, Context context)
    {
        init(context);

        try {
            Log.d("Settings", data.getBoolean(key) + " " + key);
            return data.getBoolean(key);
        } catch (Exception e) {
            Log.d("Settings", def + " " + key);
            return def;
        }
    }

    @Nullable
    public static String getString(String key, Context context)
    {
        init(context);

        try {
            return data.getString(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getInt(String key, int def, Context context)
    {
        init(context);

        try {
            return data.getInt(key);
        } catch (Exception e) {
            return def;
        }
    }

}
