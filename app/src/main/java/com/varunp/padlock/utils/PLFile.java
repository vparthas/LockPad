package com.varunp.padlock.utils;

public class PLFile
{
    public String getFileName() { return fileName; }
    public String getFolderName() { return folderName; }
    public String getSuffix() { return suffix; }

    private String fileName, folderName, suffix;

    public PLFile(String name)
    {
        fileName = name;

        if(name.contains("|") && name.contains("."))
        {
            int delim = name.indexOf(Globals.FILE_DELIM);
            int suff = name.lastIndexOf(".");
            fileName = name.substring(delim + 1, suff);
            folderName = name.substring(0, delim);
            suffix = name.substring(suff);
        }
        else
        {
            folderName = null;
            suffix = null;
        }
    }

    public PLFile(PLFile other)
    {
        this.fileName = new String(other.fileName);
        this.folderName = new String(other.folderName);
        this.suffix = new String(other.suffix);
    }

    public boolean isFolder()
    {
        return folderName == null && suffix == null;
    }

    public String getRawNamw()
    {
        return folderName + Globals.FILE_DELIM + fileName + suffix;
    }
}