package com.varunp.padlock.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

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
}
