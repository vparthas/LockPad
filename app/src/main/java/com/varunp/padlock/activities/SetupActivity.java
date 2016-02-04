package com.varunp.padlock.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.varunp.padlock.R;
import com.varunp.padlock.utils.FileManager;
import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.JsonWrapper;
import com.varunp.padlock.utils.PasswordEncryptionService;

import net.dealforest.sample.crypt.AES256Cipher;

import org.json.JSONObject;

public class SetupActivity extends AppCompatActivity implements TextWatcher {

    private boolean fab_enabled;
    private FloatingActionButton fab;

    private EditText password1, password2, recoveryq, recoverya;
    private TextView pwmatch, recoveryLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initToolBar();

        initFab();

        initViews();
    }

    private void initViews()
    {
        password1 = (EditText)findViewById(R.id.password_setup_input);
        password1.addTextChangedListener(this);
        password2 = (EditText)findViewById(R.id.password_setup_input_repeat);
        password2.addTextChangedListener(this);
        recoveryq = (EditText)findViewById(R.id.recovery_question_input);
        recoveryq.addTextChangedListener(this);
        recoverya = (EditText)findViewById(R.id.recovery_answer_input);
        recoverya.addTextChangedListener(this);

        pwmatch = (TextView)findViewById(R.id.password_match_view);
        recoveryLength = (TextView)findViewById(R.id.recovery_answer_view);
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Setup");
        setTitle("Setup");
    }

    private void initFab()
    {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        setFabEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fab_enabled)
                    Snackbar.make(view, "Setup incomplete", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                else
                {
                    boolean success = commitPasswordData(view) || commitRecoveryData(view);
                    if(success)
                        finish();
                    else
                        Snackbar.make(view, "Save failed. Please try again.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                }
            }
        });
    }

    private void setFabEnabled(boolean state)
    {
        if (state)
        {
            fab.setBackgroundTintList(getResources().getColorStateList(R.color.setup_submit_enabled));
            fab.setImageResource(R.drawable.check);
        }
        else
        {
            fab.setBackgroundTintList(getResources().getColorStateList(R.color.setup_submit_disabled));
            fab.setImageResource(R.drawable.close);
        }

        fab_enabled = state;
    }

    private boolean commitRecoveryData(View view)
    {
        String encryptedPass = AES256Cipher.encrypt(password1.getText().toString(), AES256Cipher.generateKey(recoverya.getText().toString()));
        String recoveryQuestion = recoveryq.getText().toString(); //no need to encrypt
        byte[] recoveryAnsHash, recoveryAnsSalt;
        try
        {
            recoveryAnsSalt = PasswordEncryptionService.generateSalt();
            recoveryAnsHash = PasswordEncryptionService.getEncryptedPassword(recoverya.getText().toString(), recoveryAnsSalt);
        }
        catch (Exception e)
        {
            Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return false;
        }

        JSONObject data = JsonWrapper.createRecoveryData(recoveryAnsHash, recoveryAnsSalt, recoveryQuestion, encryptedPass);
        if(data == null)
            return false;

        boolean status = new FileManager(this).saveFile(true, Globals.FILENAME_RECOVERY_INFO, data.toString());
        return status;
    }

    private boolean commitPasswordData(View view)
    {
        byte[] salt, passwordHash;

        try
        {
            salt = PasswordEncryptionService.generateSalt();
            passwordHash = PasswordEncryptionService.getEncryptedPassword(password1.getText().toString(), salt);
        }
        catch (Exception e)
        {
            Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return false;
        }

        JSONObject loginDataJSON = JsonWrapper.createLoginData(passwordHash, salt);
        if(loginDataJSON == null)
        {
            Snackbar.make(view, "Failed to create JSON", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return false;
        }

        boolean status = new FileManager(this).saveFile(true, Globals.FILENAME_LOGIN_INFO, loginDataJSON.toString());
        return status;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        String pw = password1.getText().toString(), pw2 = password2.getText().toString(),
                req = recoveryq.getText().toString(), rea = recoverya.getText().toString();

        boolean enabledState = false;

        if (pw.isEmpty() || pw2.isEmpty())
        {
            pwmatch.setText("Enter password");
            pwmatch.setTextColor(Color.RED);
        }
        else if (!(pw.equals(pw2)))
        {
            pwmatch.setText("Passwords do not match");
            pwmatch.setTextColor(Color.RED);
        }
        else if(pw.length() < 6)
        {
            pwmatch.setText("Password must be at least 6 characters in length");
            pwmatch.setTextColor(Color.RED);
        }
        else
        {
            pwmatch.setText("Good");
            pwmatch.setTextColor(Color.GREEN);
            enabledState = true;
        }

        if(req.isEmpty() || rea.isEmpty())
        {
            recoveryLength.setText("Enter recovery question and answer");
            recoveryLength.setTextColor(Color.RED);
            enabledState = false;
        }
        else if(rea.length() < 6)
        {
            recoveryLength.setText("Recovery answer must be at least 6 characters in length");
            recoveryLength.setTextColor(Color.RED);
            enabledState = false;
        }
        else
        {
            recoveryLength.setText("Good");
            recoveryLength.setTextColor(Color.GREEN);
        }

        setFabEnabled(enabledState);
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
}
