package com.varunp.lockpad.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.varunp.lockpad.R;
import com.varunp.lockpad.utils.settings.SettingsManager;

import net.dealforest.sample.crypt.AES256Cipher;

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
