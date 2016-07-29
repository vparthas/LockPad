package com.varunp.padlock.utils.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Varun on 1/14/2016.
 */
public class FileManager
{
    public static final boolean FILE_INTERNAL = true;
    public static final boolean FILE_EXTERNAL = false;

    Context m_context;

    public FileManager(Context ctx)
    {
        m_context = ctx;
    }

    public String readFile(boolean internal, String path)
    {
        if(internal)
            return readFile(m_context.getFilesDir() + "/" + path);
        else
            return readFile(path);
    }

    private String readFile(String name)
    {
        String ret = "";

        File f = new File(name);
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(f));
            for(String line = br.readLine(); line != null; line = br.readLine())
            {
                ret += line + "\n";
            }
        }
        catch (Exception e)
        {
            return null;
        }

        return ret;
    }

    public boolean saveFile(boolean internal, String name, String data)
    {
        File file = internal ? new File(m_context.getFilesDir() + "/" + name) : new File(name);
        FileOutputStream outputStream;
        try
        {
            file.createNewFile();

            outputStream = new FileOutputStream(file, false);
            outputStream.write(data.getBytes());
            outputStream.close();

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean delete(boolean internal, String name)
    {
        File file = internal ? new File(m_context.getFilesDir() + "/" + name) : new File(name);
        return file.delete();
    }

    public File[] getContents(boolean internal, String dir)
    {
        return internal ? new File(m_context.getFilesDir() + "/" + dir).listFiles() :
                new File(dir).listFiles();
    }

    public String[] getContentsStr(boolean internal, String dir)
    {
        File[] files = getContents(internal, dir);
        String[] ret = new String[files.length];

        for(int i = 0; i < files.length; i++)
            ret[i] = files[i].getName();

        return ret;
    }

    public String getInternalPath(String dir)
    {
        return m_context.getFilesDir() + "/" + dir;
    }

    public boolean createFolder(String dir)
    {
        File f = new File(dir);
        return f.mkdirs();
    }

    public boolean exists(boolean internal, boolean folder, String dir)
    {
        File f =  new File(internal ? getInternalPath(dir) : dir);
        return f.exists() && (folder ? f.isDirectory() : !f.isDirectory());
    }

    public boolean renameFile(boolean internal, String oldDir, String newDir)
    {
        String path = m_context.getFilesDir() + "/";
        if(internal)
            return rename(path + oldDir, path + newDir);
        else
            return rename(oldDir, newDir);
    }

    private boolean rename(String oldDir, String newDir)
    {
        File file = new File(oldDir);
        return file.renameTo(new File(newDir));
    }
}
