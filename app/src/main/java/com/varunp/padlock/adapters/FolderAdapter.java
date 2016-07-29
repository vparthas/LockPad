package com.varunp.padlock.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.varunp.padlock.R;
import com.varunp.padlock.utils.file.FileTracker;
import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.file.PLFile;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder>
{
    private boolean hasParent;

    private List<PLFile> mDataset;
    private FolderAdapterListener listener;
    private String mQuery;

    private String focused;

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

    public static final int QUERY_FOLDERS = 0, QUERY_FILES = 1, QUERY_TEXT = 2, QUERY_IMAGE = 3;

    // Provide a suitable constructor (depends on the kind of dataset)
//    public FolderAdapter(int query, FolderAdapterListener listener)
//    {
//        hasParent = false;
//        this.listener = listener;
//        refresh(query + "");
//    }

    public FolderAdapter(String query, FolderAdapterListener listener)
    {
        hasParent = false;
        this.listener = listener;
        refresh(query);
    }

    public boolean hasParent()
    {
        return hasParent;
    }

    public void up()
    {
        refresh(QUERY_FOLDERS + "");
        hasParent = false;
    }

    public void down(PLFile folder)
    {
        refresh(folder.getFileName());
        hasParent = true;
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
        holder.nameView.setText(mDataset.get(position).getFileName());
        holder.itemView.setText(getSubLine(mDataset.get(position)));
        holder.iconView.setImageResource(getImage(mDataset.get(position)));

        if(position == 1)
            holder.mView.setClickable(true);

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mDataset.get(position).isFolder())
                {
                    down(mDataset.get(position));
                }
                else
                    listener.open(mDataset.get(position));
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                listener.longPress(mDataset.get(position));
                return true;
            }
        });
    }

    private void refresh(String query)
    {
        mQuery = query;
        refresh();
    }

    public void refresh()
    {
        try
        {
            int q = Integer.parseInt(mQuery);
            switch(q)
            {
                case QUERY_FOLDERS:
                    mDataset = FileTracker.getFolders();
                    listener.onQueryChanged(Globals.NAV_HEADER_FOLDERS, getItemCount());
                    break;
                case QUERY_FILES:
                    mDataset = FileTracker.getAllFiles();
                    listener.onQueryChanged(Globals.NAV_HEADER_FILES, getItemCount());
                    break;
                case QUERY_TEXT:
                    mDataset = FileTracker.getFileType(Globals.FILENAME_TEXT);
                    listener.onQueryChanged(Globals.NAV_HEADER_NOTES, getItemCount());
                    break;
                case QUERY_IMAGE:
                    mDataset = FileTracker.getFileType(Globals.FILENAME_IMAGE);
                    listener.onQueryChanged(Globals.NAV_HEADER_IMAGES, getItemCount());
                    break;
            }
            focused = null;
        }
        catch (NumberFormatException e)
        {
            hasParent = true;
            focused = mQuery;
            mDataset = FileTracker.getFolder(new PLFile(mQuery));
            listener.onQueryChanged(mQuery, getItemCount());
        }

        notifyDataSetChanged();
    }

    public static final int FOLDER = R.drawable.folder;
    public static final int NOTE = R.drawable.file_document;
    public static final int IMAGE = R.drawable.file_image;
    private int getImage(PLFile plFile)
    {
        if(plFile.isFolder())
            return FOLDER;
        if(plFile.getSuffix().equals(Globals.FILENAME_TEXT))
            return NOTE;
        if(plFile.getSuffix().equals(Globals.FILENAME_IMAGE))
            return IMAGE;
        else
            return android.R.drawable.ic_dialog_alert;
    }

    private String getSubLine(PLFile plFile)
    {
        if(plFile.isFolder())
        {
            int temp = FileTracker.getFolder(plFile).size();
            return temp == 1 ? temp + " item" : temp + " items";
        }
        if(plFile.getSuffix().equals(Globals.FILENAME_IMAGE))
            return "Image";
        if(plFile.getSuffix().equals(Globals.FILENAME_TEXT))
            return "Text";
        else
            return "Error!";
    }

    public String getFocused()
    {
        return focused == null ? "New Folder" : focused;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }
}