package com.varunp.padlock.utils.password;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.file.FileManager;

import net.dealforest.sample.crypt.AES256Cipher;

/**
 * Created by Varun on 6/18/2016.
 */
public class PasswordRecoveryManager
{

    FileManager fileManager;
    Context context;

    JsonWrapper.RecoveryData data;

    public PasswordRecoveryManager(Context context)
    {
        this.context = context;
        fileManager = new FileManager(context);

        loadData();
    }

    public JsonWrapper.RecoveryData getData()
    {
        return data;
    }

    public boolean recoveryValid(String attempt)
    {
        try
        {
            return PasswordEncryptionService.authenticate(attempt, data.hash, data.salt);
        }
        catch (Exception e)
        {
            Log.d(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    public void recover(String answer, String newPass) throws Exception
    {
        byte[] key = AES256Cipher.decryptToByte(data.password, AES256Cipher.generateKey(answer));

        PasswordChanger changer = new PasswordChanger(context);
        changer.changePassword(key, newPass);
        loadData();
    }

    private void loadData()
    {
        String rawData = fileManager.readFile(true, Globals.FILENAME_RECOVERY_INFO);
        data = JsonWrapper.readRecoveryData(rawData);
    }
}
