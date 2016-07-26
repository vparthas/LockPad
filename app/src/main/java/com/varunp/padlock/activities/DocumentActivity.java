package com.varunp.padlock.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.varunp.padlock.R;
import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.file.FileManager;
import com.varunp.padlock.utils.file.FileTracker;
import com.varunp.padlock.utils.file.PLFile;

import net.dealforest.sample.crypt.AES256Cipher;

public class DocumentActivity extends AppCompatActivity
{
    public static final String INTENT_KEY_IS_NEW_DOC = "NEW_DOC";
    public static final String INTENT_KEY_FILE_NAME = "FILE_NAME";
    public static final String INTENT_KEY_FOLDER_NAME = "FOLDER_NAME";
    public static final String INTENT_KEY_ENCRYPTION_KEY = "ENC_KEY";

    private boolean editMode;
    private FloatingActionButton fab;
    private EditText documentInput;

    private String folderName, fileName;
    FileManager fileManager;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileManager = new FileManager(getApplicationContext());

        initViews();
        initFab();
        initDocument();
    }

    private void initViews() //from the 6
    {
        documentInput = (EditText)findViewById(R.id.doc_input);
        documentInput.setEnabled(false);
    }

    private void initFab()
    {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(documentInput.getText().toString().isEmpty())
                    return;

                setEditMode(!editMode);
                if(!editMode)
                    saveChanges();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initDocument()
    {
        AES256Cipher.setKey(this.getIntent().getByteArrayExtra(INTENT_KEY_ENCRYPTION_KEY));

        boolean flag = this.getIntent().getBooleanExtra(INTENT_KEY_IS_NEW_DOC, true);
        folderName = this.getIntent().getStringExtra(INTENT_KEY_FOLDER_NAME);
        fileName = flag ? "New Document" : this.getIntent().getStringExtra(INTENT_KEY_FILE_NAME);

        setFileName(folderName, fileName);
        setEditMode(flag);

        if(!flag)
            readFile();
    }

    private void setEditMode(boolean /*bickin back bein*/ bool)
    {
        editMode = bool;

        documentInput.setEnabled(editMode);

        if(editMode)
        {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.check));
            Snackbar.make(documentInput, "Press back to discard changes.", Snackbar.LENGTH_SHORT).show();
        }
        else
        {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_edit));
        }
    }

    private void readFile()
    {
        String file = PLFile.generateFileName(folderName, fileName, Globals.FILENAME_TEXT);
        String raw = fileManager.readFile(true, Globals.FOLDER_DATA + "/" + file);
        String decrypt = AES256Cipher.decrypt(raw, AES256Cipher.getKey());
        documentInput.setText(decrypt);
    }

    private void saveChanges()
    {
        Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();

        if(documentInput.getText().toString().isEmpty())
        {
            deleteFile();
            return;
        }

        String raw = documentInput.getText().toString();
        String enc = AES256Cipher.encrypt(raw, AES256Cipher.getKey());

        String fn = PLFile.generateFileName(folderName, fileName, Globals.FILENAME_TEXT);
        int i = 0;
        while(fileManager.exists(true, false, Globals.FOLDER_DATA + "/" + fn))
        {
            fn = PLFile.generateFileName(folderName, fileName + " (" + ++i + ")", Globals.FILENAME_TEXT);
        }
        setFileName(folderName, i == 0 ? fileName : fileName + " (" + i + ")");

        fileManager.saveFile(true, Globals.FOLDER_DATA + "/" + fn, enc);
        FileTracker.addFile(getApplicationContext(), fn);
    }

    private void setFileName(String folder, String file)
    {
        folderName = folder;
        fileName = file;
        setTitle(Html.fromHtml("<small>" + folderName + "/<strong>" + fileName + "</strong></small>"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_document, menu);

        MenuItem item = menu.findItem(R.id.action_share_file);
        item.setTitle("Copy contents");

        return true;
    }

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
            //TODO
            return true;
        }

        if(id == R.id.action_share_file)
        {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(fileName, documentInput.getText().toString());
            clipboard.setPrimaryClip(clip);
            return true;
        }

        if(id == R.id.action_delete_file)
        {
            showDeleteDialog();
            return true;
        }

        return false;
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
        setEditMode(false);
        String fn = PLFile.generateFileName(folderName, fileName, Globals.FILENAME_TEXT);
        fileManager.delete(true, fn);
        FileTracker.removeFile(getApplicationContext(), new PLFile(fn));
        onBackPressed();
    }

    @Override
    public void onBackPressed()
    {
        if(editMode && !documentInput.getText().toString().isEmpty())
        {
            showDiscardDialog();
            return;
        }

        Intent back = new Intent(this, MainActivity.class);
        back.putExtra(INTENT_KEY_ENCRYPTION_KEY, AES256Cipher.getKey());
        back.putExtra(MainActivity.INTENT_FOLDER, folderName);
        startActivity(back);
    }

    private void showDiscardDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to discard changes to this file?")
                .setTitle("Discard changes?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                setEditMode(false);
                readFile();
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

    @Override
    protected void onPause()
    {
        super.onPause();

        if(editMode)
            saveChanges();

        AES256Cipher.setKey(null);
        finish();
    }
}
