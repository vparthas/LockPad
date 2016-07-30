package com.varunp.lockpad.utils.password;

import android.content.Context;
import android.util.Log;

import com.varunp.lockpad.utils.Globals;
import com.varunp.lockpad.utils.file.FileManager;

import net.dealforest.sample.crypt.AES256Cipher;

import org.json.JSONObject;

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

    public boolean changeRecoveryInfo(String oldAns, String question, String newAns)
    {
        byte[] key = AES256Cipher.decryptToByte(data.password, AES256Cipher.generateKey(oldAns));
        byte[] keyEnc = AES256Cipher.encryptToByte(key, AES256Cipher.generateKey(newAns));

        byte[] recoveryAnsHash, recoveryAnsSalt;
        try
        {
            recoveryAnsSalt = PasswordEncryptionService.generateSalt();
            recoveryAnsHash = PasswordEncryptionService.getEncryptedPassword(newAns, recoveryAnsSalt);

            JSONObject json = JsonWrapper.createRecoveryData(recoveryAnsHash, recoveryAnsSalt, question, keyEnc);
            return fileManager.saveFile(true, Globals.FILENAME_RECOVERY_INFO, json.toString());
        }
        catch (Exception e)
        {
            Log.d("PWR", e.getMessage());
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
