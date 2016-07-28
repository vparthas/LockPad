package com.varunp.padlock.adapters;

import android.graphics.Color;

import com.varunp.padlock.utils.file.PLFile;

/**
 * Created by Varun on 6/18/2016.
 */
public interface FolderAdapterListener
{
    void onQueryChanged(String query, int itemCount);

    void open(PLFile file);

    void longPress(PLFile file);
}
