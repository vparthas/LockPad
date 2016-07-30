package com.varunp.lockpad.activities;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.varunp.lockpad.R;
import com.varunp.lockpad.utils.Globals;
import com.varunp.lockpad.utils.file.FileManager;
import com.varunp.lockpad.utils.file.FileTracker;
import com.varunp.lockpad.utils.file.PLFile;
import com.varunp.lockpad.utils.password.JsonWrapper;
import com.varunp.lockpad.utils.password.PasswordChanger;
import com.varunp.lockpad.utils.password.PasswordEncryptionService;
import com.varunp.lockpad.utils.settings.SettingsManager;

import net.dealforest.sample.crypt.AES256Cipher;

import java.util.List;

public class SettingsActivity extends AppCompatActivity
{
    private boolean cop_orig;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        cop_orig = SettingsManager.getBoolean(SettingsManager.CLOSE_ON_PAUSE, true, getApplicationContext());

        if(AES256Cipher.getKey() == null)
            AES256Cipher.setKey(getIntent().getByteArrayExtra(DocumentActivity.INTENT_KEY_ENCRYPTION_KEY));

        initViews();
    }

    private void initViews()
    {
        Switch closeOnPause = (Switch)findViewById(R.id.settings_close_on_pause);
        closeOnPause.setChecked(cop_orig);
        closeOnPause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SettingsManager.putBoolean(SettingsManager.CLOSE_ON_PAUSE, isChecked, getApplicationContext());
            }
        });

        Button changePass = (Button)findViewById(R.id.settings_change_pass);
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showChangePassDialog();
            }
        });

        Button changeRecov = (Button)findViewById(R.id.settings_change_recov);
        changeRecov.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showChangeRecovDialog();
            }
        });
    }

    private void showChangePassDialog()
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_pass);
        dialog.setCancelable(true);

        final TextView errorView = (TextView)dialog.findViewById(R.id.dialog_change_pass_error);

        final EditText newPass1 = (EditText)dialog.findViewById(R.id.dialog_change_pass_newPass1);
        final EditText newPass2 = (EditText)dialog.findViewById(R.id.dialog_change_pass_newPass2);
        final EditText oldPass = (EditText)dialog.findViewById(R.id.dialog_change_pass_oldPass);

        FileManager fm = new FileManager(getApplicationContext());
        String loginDataRaw = fm.readFile(FileManager.FILE_INTERNAL, Globals.FILENAME_LOGIN_INFO);
        final byte[][] m_passwordData = JsonWrapper.readLoginData(loginDataRaw);

//        View.OnFocusChangeListener fieldChecker = new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus)
//            {
////                Log.d("PWC", hasFocus + " " + v.toString());
//                if(!hasFocus)
//                {
//                    Pair<Boolean, String> status = checkFields(oldPass.getText().toString(),
//                            newPass1.getText().toString(),
//                            newPass2.getText().toString(),
//                            m_passwordData);
////                    okButton.setEnabled(status.first);
//
//                    String error = status.second;
//                    if(error.isEmpty())
//                        errorView.setVisibility(View.GONE);
//                    else
//                        errorView.setText(error);
//                }
//            }
//        };
//
//        oldPass.setOnFocusChangeListener(fieldChecker);
//        newPass1.setOnFocusChangeListener(fieldChecker);
//        newPass2.setOnFocusChangeListener(fieldChecker);

        final Button okButton = (Button)dialog.findViewById(R.id.dialog_change_pass_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Pair<Boolean, String> status = checkFields(oldPass.getText().toString(),
                        newPass1.getText().toString(),
                        newPass2.getText().toString(),
                        m_passwordData);

                String error = status.second;
                if(error.isEmpty())
                    errorView.setVisibility(View.GONE);
                else
                    errorView.setText(error);

                if(!status.first)
                    return;

                if(changePassword(oldPass.getText().toString(), newPass1.getText().toString(), m_passwordData))
                    dialog.dismiss();
                else
                    Toast.makeText(getApplicationContext(), "Error saving data", Toast.LENGTH_SHORT).show();
            }
        });

        Button cancelButton = (Button)dialog.findViewById(R.id.dialog_change_pass_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private Pair<Boolean, String> checkFields(String oldp, String newp1, String newp2, byte[][] m_passwordData)
    {
        if(oldp.isEmpty() || newp1.isEmpty() || newp2.isEmpty())
            return new Pair<Boolean, String>(false, "Fields cannot be blank.");

        boolean auth = false;
        try {
            if(PasswordEncryptionService.authenticate(oldp, m_passwordData[0], m_passwordData[1]))
                auth = true;
        } catch (Exception e) {}

        if(!auth)
            return new Pair<Boolean, String>(false, "Password incorrect.")  ;

        if (!newp1.equals(newp2))
            return new Pair<Boolean, String>(false, "Passwords do not match");

        else if(newp1.length() < 6)
            return new Pair<Boolean, String>(false, "Password must be at least 6 characters in length");

        return new Pair<Boolean, String>(true, "");
    }

    private boolean changePassword(String oldPass, String newPass, byte[][] m_passwordData)
    {
        PasswordChanger changer = new PasswordChanger(getApplicationContext());
        try
        {
            changer.changePassword(AES256Cipher.getKey(), newPass);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void showChangeRecovDialog()
    {

    }

    @Override
    public void onBackPressed()
    {
        if(!cop_orig && !SettingsManager.getBoolean(SettingsManager.CLOSE_ON_PAUSE, true, getApplicationContext()))
        {
            super.onBackPressed();
            return;
        }

        Log.d("COP", "reached");
        Intent back = new Intent(this, MainActivity.class);
        back.putExtra(DocumentActivity.INTENT_KEY_ENCRYPTION_KEY, AES256Cipher.getKey());
        startActivity(back);
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

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause()
    {
        super.onPause();

        if(SettingsManager.getBoolean(SettingsManager.CLOSE_ON_PAUSE, true, getApplicationContext()))
        {
            AES256Cipher.setKey(null);
            finish();
        }
    }
}
