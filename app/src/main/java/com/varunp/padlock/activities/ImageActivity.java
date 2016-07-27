package com.varunp.padlock.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.varunp.padlock.R;
import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.file.FileManager;
import com.varunp.padlock.utils.file.FileTracker;
import com.varunp.padlock.utils.file.ImageUtils;
import com.varunp.padlock.utils.file.PLFile;

import net.dealforest.sample.crypt.AES256Cipher;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ImageActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private String fileName, folderName;
    private FileManager fileManager;
    private String imgData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        fileName = getIntent().getStringExtra(DocumentActivity.INTENT_KEY_FILE_NAME);
        folderName = getIntent().getStringExtra(DocumentActivity.INTENT_KEY_FOLDER_NAME);
        AES256Cipher.setKey(getIntent().getByteArrayExtra(DocumentActivity.INTENT_KEY_ENCRYPTION_KEY));

        setTitle(fileName);

        fileManager = new FileManager(getApplicationContext());

        String filepath = getFilePath(fileName, folderName);
        imgData = fileManager.readFile(true, filepath);
        String raw = AES256Cipher.decrypt(imgData, AES256Cipher.getKey());
        Bitmap bmp = ImageUtils.decodeBase64(raw);
        setImage(bmp);
        setTitle();
    }

    private String getFilePath(String file, String folder)
    {
        return Globals.FOLDER_DATA + "/" + PLFile.generateFileName(folder, file, Globals.FILENAME_IMAGE);
    }

    private void setImage(Bitmap bmp)
    {
        ((ImageView)mContentView).setImageBitmap(bmp);
    }

    private void setTitle()
    {
        setTitle(Html.fromHtml("<small>" + folderName + "/<strong>" + fileName + "</strong></small>"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_document, menu);

        MenuItem item = menu.findItem(R.id.action_share_file);
        item.setTitle("Un-hide file");

        return true;
    }

    @Override
    public void onBackPressed()
    {
        Intent back = new Intent(this, MainActivity.class);
        back.putExtra(DocumentActivity.INTENT_KEY_ENCRYPTION_KEY, AES256Cipher.getKey());
        back.putExtra(MainActivity.INTENT_FOLDER, folderName);
        startActivity(back);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        AES256Cipher.setKey(null);
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            this.onBackPressed();
            return true;
        }

        if(id == R.id.action_edit_title)
        {
            showNameDialog();
            return true;
        }

        if(id == R.id.action_share_file)
        {
            showUnhideDialog();
            return true;
        }

        if(id == R.id.action_delete_file)
        {
            showDeleteDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUnhideDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to un-hide this file? The file will be de-encrypted and saved to storage.")
                .setTitle("Un-hide file?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                String dec = AES256Cipher.decrypt(imgData, AES256Cipher.getKey());
                Bitmap bmp = ImageUtils.decodeBase64(dec);

                boolean result = ImageUtils.writeToFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + fileName + ".png", bmp);
                if(result)
                {
                    Toast.makeText(getApplicationContext(), "File saved to storage.", Toast.LENGTH_SHORT).show();
                    deleteFile();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Error saving file to storage.", Toast.LENGTH_SHORT).show();
                }

                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNameDialog()
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_name);
        dialog.setCancelable(true);

        final EditText editText_name = (EditText)dialog.findViewById(R.id.dialog_change_name_file);
        final AutoCompleteTextView editText_folder = (AutoCompleteTextView)dialog.findViewById(R.id.autoCompleteTextView_folder);

        editText_name.setText(fileName);
        editText_folder.setText(folderName);

        List<PLFile> folders = FileTracker.getFolders();
        String[] strList = new String[folders.size()];
        for(int i = 0; i < folders.size(); i++)
            strList[i] = folders.get(i).getRawName();

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, strList);
        editText_folder.setAdapter(adapter);
        editText_folder.setThreshold(1);

        Button button_ok = (Button)dialog.findViewById(R.id.dialog_change_name_ok);
        button_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String newFileName = editText_name.getText().toString();
                String newFolderName = editText_folder.getText().toString();

                if(newFileName.isEmpty() || newFolderName.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),
                            "File and folder name cannot be blank.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(newFileName.contains(Globals.FILE_DELIM) || newFolderName.contains(Globals.FILE_DELIM))
                {
                    Toast.makeText(getApplicationContext(),
                            "File and folder name cannot contain " + Globals.FILE_DELIM + ".",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!newFileName.equals(fileName) || !newFolderName.equals(folderName))
                {
                    deleteFile(fileName, folderName);

                    fileName = newFileName;
                    folderName = newFolderName;
                    saveChanges();

                    dialog.cancel();
                }
                dialog.dismiss();
            }
        });

        Button button_cancel = (Button)dialog.findViewById(R.id.dialog_change_name_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveChanges()
    {
        String fn = PLFile.generateFileName(folderName, fileName, Globals.FILENAME_IMAGE);
        int i = 0;
        while(fileManager.exists(true, false, Globals.FOLDER_DATA + "/" + fn))
        {
            fn = PLFile.generateFileName(folderName, fileName + " (" + ++i + ")", Globals.FILENAME_TEXT);
        }
        setFileName(folderName, i == 0 ? fileName : fileName + " (" + i + ")");

        fileManager.saveFile(true, Globals.FOLDER_DATA + "/" + fn, imgData);
        FileTracker.addFile(getApplicationContext(), fn);
    }

    private void setFileName(String folder, String file)
    {
        folderName = folder;
        fileName = file;
        setTitle();
    }

    private void showDeleteDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to delete this file?")
                .setTitle("Delete file?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                deleteFile();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteFile()
    {
        deleteFile(fileName, folderName);
        onBackPressed();
    }

    private void deleteFile(String file, String folder)
    {
        String fn = PLFile.generateFileName(folder, file, Globals.FILENAME_IMAGE);
        fileManager.delete(true, fn);
        FileTracker.removeFile(getApplicationContext(), new PLFile(fn));
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
