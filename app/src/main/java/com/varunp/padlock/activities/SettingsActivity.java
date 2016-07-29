package com.varunp.padlock.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.varunp.padlock.R;
import com.varunp.padlock.utils.settings.SettingsManager;

import net.dealforest.sample.crypt.AES256Cipher;

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(AES256Cipher.getKey() == null)
            AES256Cipher.setKey(getIntent().getByteArrayExtra(DocumentActivity.INTENT_KEY_ENCRYPTION_KEY));

        initViews();
    }

    private void initViews()
    {
        
    }

    @Override
    public void onBackPressed()
    {
        if(!SettingsManager.getBoolean(SettingsManager.CLOSE_ON_PAUSE, true, getApplicationContext()))
        {
            super.onBackPressed();
            return;
        }

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
