package com.varunp.lockpad.utils.file;

import android.content.Context;
import android.util.Log;

import com.varunp.lockpad.utils.Globals;

import org.json.JSONArray;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Varun on 6/13/2016.
 */
public class FolderList
{
    public static Set<String> read(Context context)
    {
        FileManager fileManager = new FileManager(context);
        String rawJSON = fileManager.readFile(true, Globals.FILENAME_FOLDER_LIST);

        Set<String> folderSet = new HashSet<>();
        try
        {
            JSONArray jsonArray = new JSONArray(rawJSON);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                String j = jsonArray.getString(i);
                folderSet.add(j);
            }
        }
        catch (Exception e)
        {
            Log.d("FolderList", e.getMessage());
        }

        return folderSet;
    }

    public static void commit(Context context, Set<String> folderSet)
    {
        FileManager fileManager = new FileManager(context);

        JSONArray out = new JSONArray();
        for(String folder : folderSet)
            out.put(folder);

        fileManager.saveFile(FileManager.FILE_INTERNAL, Globals.FILENAME_FOLDER_LIST, out.toString());
    }
}
