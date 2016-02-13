package com.varunp.padlock.adapters;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.varunp.padlock.R;
import com.varunp.padlock.utils.Globals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder>
{
    private FileItem[] mDataset;

    private class FileItem
    {
        public static final int FOLDER = R.drawable.folder;
        public static final int NOTE = R.drawable.file_document;
        public static final int IMAGE = R.drawable.file_image;

        private String name, subLine;
        private int fileType;
        private File file;

        public FileItem(File f)
        {
            if(f.isDirectory())
            {
                fileType = FOLDER;
                int i = f.listFiles().length;
                subLine = i != 1 ? i + " items" : i + " item";
            }
            else if(f.getName().endsWith(Globals.IMAGE_SUFFIX))
            {
                fileType = IMAGE;
                subLine = "";
            }
            else
            {
                fileType = NOTE;
                subLine = "";
            }

            name = f.getName();
            file = f;
        }

        public FileItem(String parent)
        {
            name = "Back";
            subLine = "..";
            fileType = FOLDER;
            file = new File(parent);
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private View mView;
        private TextView nameView, itemView;
        private ImageView iconView;

        public ViewHolder(View v)
        {
            super(v);
            mView = v;

            nameView = (TextView)mView.findViewById(R.id.pathView);
            itemView = (TextView)mView.findViewById(R.id.subtitleView);
            iconView = (ImageView)mView.findViewById(R.id.iconView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FolderAdapter(String path)
    {
        mDataset = getFileItems(new String[]{path});
    }

    public FolderAdapter(String[] paths)
    {
        mDataset = getFileItems(paths);
    }

    private FileItem[] getFileItems(String[] files)
    {
        List<FileItem> ret = new ArrayList<FileItem>();
        for(int i = 0; i < files.length; i++)
        {
            if(files[i].startsWith(Globals.FOLDER_PARENT))
            {
                ret.add(new FileItem(files[i].substring(2)));
            }
            File temp = new File(files[i]);
            for(File f : temp.listFiles())
                ret.add(new FileItem(f));
        }
        return ret.toArray(new FileItem[ret.size()]);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_view_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.nameView.setText(mDataset[position].name);
        holder.itemView.setText(mDataset[position].subLine);
        holder.iconView.setImageResource(mDataset[position].fileType);

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mDataset[position].fileType == FileItem.FOLDER)
                {
                    if(mDataset[position].subLine.equals(Globals.FOLDER_PARENT))
                        mDataset = getFileItems(new String[]
                                {mDataset[position].file.getAbsolutePath()});
                    else
                        mDataset = getFileItems(new String[]
                                {Globals.FOLDER_PARENT + mDataset[position].file.getParent(),
                                        mDataset[position].file.getAbsolutePath()});
                    notifyDataSetChanged();
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return mDataset.length;
    }
}