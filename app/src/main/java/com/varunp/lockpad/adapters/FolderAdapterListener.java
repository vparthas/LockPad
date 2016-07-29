package com.varunp.lockpad.adapters;

import com.varunp.lockpad.utils.file.PLFile;

/**
 * Created by Varun on 6/18/2016.
 */
public interface FolderAdapterListener
{
    void onQueryChanged(String query, int itemCount);

    void open(PLFile file);

    void longPress(PLFile file);
}
