package com.varunp.padlock.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.varunp.padlock.utils.FileManager;
import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.JsonWrapper;
import com.varunp.padlock.R;
import com.varunp.padlock.utils.PasswordEncryptionService;

import net.dealforest.sample.crypt.AES256Cipher;

public class LoginActivity extends AppCompatActivity {

    FileManager m_fm;
    byte[][] m_passwordData;

    EditText m_passwordEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        initViews();
    }

    private void initViews()
    {
        m_passwordEntry = (EditText)findViewById(R.id.login_entry);
        m_passwordEntry.setOnEditorActionListener(new EditText.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    Snackbar.make(view, "Checking Password...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    checkPassword(m_passwordEntry.getText().toString(), view);
                    return true;
                }
                return false;
            }
        });
    }

    private void checkPassword(String s, View view)
    {
        boolean valid;
        try
        {
            valid = PasswordEncryptionService.authenticate(s, m_passwordData[0],
                    m_passwordData[1]);
        }
        catch (Exception e)
        {
            valid = false;
        }

        if(valid)
        {
            AES256Cipher.setKey(AES256Cipher.generateKey(s));
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else
        {
            Snackbar.make(view, "Checking Password...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        m_fm = new FileManager(getApplication());
        String loginDataRaw = m_fm.readFile(FileManager.FILE_INTERNAL, Globals.FILENAME_LOGIN_INFO);
        m_passwordData = JsonWrapper.readLoginData(loginDataRaw);

        if(m_passwordData == null)
        {
            showSetupDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_recovery_button:
                Toast.makeText(this, "Recovery Screen", Toast.LENGTH_SHORT).show(); //TODO:
                break;
        }
        return true;
    }

    private void showSetupDialog()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.setup_dialog_message))
                .setTitle(getString(R.string.setup_dialog_title));

        builder.setPositiveButton(getString(R.string.setup_dialog_okButton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        startActivity(new Intent(getApplicationContext(), SetupActivity.class));
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(getString(R.string.setup_dialog_cancelButton),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }
}
