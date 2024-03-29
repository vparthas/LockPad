package com.varunp.lockpad.utils.file;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.varunp.lockpad.utils.Globals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Varun on 6/13/2016.
 */
public class FileTracker
{
    private static HashMap<String, Set<PLFile>> files;

    public static void init(Context context, String[] fileList)
    {
        Log.d("FileTracker", fileList.length + "");

        files = new HashMap<>();

        files.put(Globals.FILENAME_IMAGE, new HashSet<PLFile>());
        files.put(Globals.FILENAME_TEXT, new HashSet<PLFile>());

        for(String s : FolderList.read(context))
        {
            Log.d("FileTracker", "folder added: " + s);
            files.put(s, new HashSet<PLFile>());
        }

        for(String file : fileList)
        {
            Log.d("FileTracker", "file added " + file);
            addFile(context, file);
        }
    }

    private static List<PLFile> getQuery(String key)
    {
        List<PLFile> ret = new ArrayList<>();
        ret.addAll(files.get(key));
        return ret;
    }

    @Nullable
    public static List<PLFile> getFolder(PLFile folder)
    {
        if(!folder.isFolder())
            return null;

        return getQuery(folder.getFileName());
    }

    @Nullable
    public static List<PLFile> getFileType(String suffix)
    {
        if(suffix.equals(Globals.FILENAME_TEXT) || suffix.equals(Globals.FILENAME_IMAGE))
            return getQuery(suffix);

        return null;
    }

    public static List<PLFile> getFolders()
    {
        ArrayList<PLFile> ret = new ArrayList<>();
        for(String s : files.keySet())
        {
            if(s.equals(Globals.FILENAME_IMAGE) || s.equals(Globals.FILENAME_TEXT))
                continue;

            ret.add(new PLFile(s));
        }
        return ret;
    }

    public static List<PLFile> getAllFiles()
    {
        ArrayList<PLFile> ret = new ArrayList<>();

        for(String key : files.keySet())
        {
            if(key.equals(Globals.FILENAME_IMAGE) || key.equals(Globals.FILENAME_TEXT))
                continue;

            for(PLFile plf : files.get(key))
            {
                ret.add(new PLFile(plf));
            }
        }

        return ret;
    }

    public static boolean addFile(Context context, String file)
    {
        PLFile plf = new PLFile(file);

        if(!files.containsKey(plf.getFolderName()))
            addFolder(context, plf.getFolderName());
        if(!files.containsKey(plf.getSuffix()))
            files.put(plf.getSuffix(), new HashSet<PLFile>());

        if(files.get(plf.getFolderName()).contains(plf))
            return false;

        files.get(plf.getFolderName()).add(plf);
        files.get(plf.getSuffix()).add(plf);

        return true;
    }

    public static void removeFile(Context context, PLFile file)
    {
        if(!files.containsKey(file.getFolderName()) || !files.containsKey(file.getSuffix()))
        {
            Log.d("FileTracker", "Unnecessary delete: " + file);
            return;
        }

        files.get(file.getFolderName()).remove(file);
        files.get(file.getSuffix()).remove(file);

        new FileManager(context).delete(FileManager.FILE_INTERNAL, Globals.FOLDER_DATA + "/" + file.getRawName());
    }

    public static boolean addFolder(Context context, String name)
    {
        return addFolder(context, name, false);
    }

    public static boolean addFolder(Context context, String name, boolean silent)
    {
        if(!checkFolderName(context, name, silent, false))
            return false;

        files.put(name, new HashSet<PLFile>());
        FolderList.commit(context, files.keySet());
        return true;
    }

    public static boolean checkFolderName(Context context, String name)
    {
        return checkFolderName(context, name, false, false);
    }

    public static boolean checkFolderName(Context context, String name, boolean silent, boolean merge)
    {
        if(name.length() == 0)
        {
            if(!silent)
                Toast.makeText(context, "Folder name cannot be blank.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(name.contains(Globals.FILE_DELIM))
        {
            if(!silent)
                Toast.makeText(context, "Folder name cannot contain '|'.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(name.matches("[0-9]+"))
        {
            if(!silent)
                Toast.makeText(context, "Folder name cannot contain only numeric digits.", Toast.LENGTH_SHORT).show(); //TODO: fix bug that requires this.
            return false;
        }

        if(files.containsKey(name) && !merge)
        {
            if(!silent)
                Toast.makeText(context, "Folder already exsists.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public static boolean folderExists(Context context, String name)
    {
        return files.containsKey(name);
    }

    public static boolean fileExists(Context context, String name)
    {
        PLFile plf = new PLFile(name);
        return files.get(Globals.FILENAME_TEXT).contains(plf)
                || files.get(Globals.FILENAME_IMAGE).contains(plf);
    }

    public static void removeFolder(Context context, String name)
    {
        removeFolder(context, name, true);
    }

    public static void removeFolder(Context context, String name, boolean delete)
    {
        if(!files.containsKey(name))
        {
            Log.d("FileTracker", "Unnecessary folder delete: " + name);
            return;
        }

        if(delete)
        {
            Set<PLFile> temp = files.get(name);
            for (PLFile plf : temp)
            {
                removeFile(context, plf);
            }
        }

        files.remove(name);
        FolderList.commit(context, files.keySet());
    }

    public static void moveFile(Context context, PLFile oldLoc, String folder)
    {
        String data = Globals.FOLDER_DATA + "/";
        String nl = folder + Globals.FILE_DELIM + oldLoc.getFileName() + oldLoc.getSuffix();

        nl = fileExists(context, nl) ?
                folder + Globals.FILE_DELIM + oldLoc.getFileName() + " - " + oldLoc.getFolderName() + oldLoc.getSuffix()
                : nl;

        int i = 1;
        while (fileExists(context, nl))
            nl = folder + Globals.FILE_DELIM + oldLoc.getFileName() + " - " + oldLoc.getFolderName() + " (" + i++ + ")" + oldLoc.getSuffix();

        new FileManager(context).renameFile(true, data + oldLoc.getRawName(), data + nl);

        files.get(oldLoc.getFolderName()).remove(oldLoc);
        files.get(oldLoc.getSuffix()).remove(oldLoc);

        addFile(context, nl);
    }
}
