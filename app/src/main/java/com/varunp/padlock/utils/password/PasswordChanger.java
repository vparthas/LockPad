package com.varunp.padlock.utils.password;

import android.content.Context;
import android.util.Log;

import com.varunp.padlock.utils.Globals;
import com.varunp.padlock.utils.file.FileManager;

import org.json.JSONObject;

import java.io.File;
import java.security.spec.ECField;

/**
 * Created by Varun on 6/18/2016.
 */
public class PasswordChanger
{
    private Context context;
    private FileManager fileManager;

    public PasswordChanger(Context context)
    {
        this.context = context;
        fileManager = new FileManager(context);
    }

    public void changePassword(String oldPass, String newPass, String recoveryAnswer) throws Exception
    {
        byte[][] loginData = JsonWrapper.readLoginData(fileManager.readFile(true, Globals.FILENAME_LOGIN_INFO));
//        Log.d("PWRecov", loginData[0] + " " + loginData[1] + " " + oldPass);
        boolean auth = false;
        try {
            auth = PasswordEncryptionService.authenticate(oldPass, loginData[JsonWrapper.LOGIN_DATA_PASSWORD],
                                                                   loginData[JsonWrapper.LOGIN_DATA_SALT]);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        if(!auth)
            throw new Exception("Old password incorrect.");

//        Log.d("PWRecov", loginData[0] + " " + loginData[1]);
        if(!createPasswordData(newPass))
            throw new Exception("Could not create password data.");

        if(!modifyRecoveryData(recoveryAnswer, newPass))
            throw new Exception("Could not modify recovery data.");

        File[] dataContents = fileManager.getContents(true, Globals.FOLDER_DATA);
        for(File file : dataContents)
        {
            String name = file.getName();
            reEncryptFile(Globals.FOLDER_DATA + "/" + name, oldPass, newPass);
        }
    }

    public boolean createPasswordData(String password)
    {
        byte[] salt, passwordHash;

        try
        {
            salt = PasswordEncryptionService.generateSalt();
            passwordHash = PasswordEncryptionService.getEncryptedPassword(password, salt);
        }
        catch (Exception e)
        {
            return false;
        }

        JSONObject loginDataJSON = JsonWrapper.createLoginData(passwordHash, salt);
        if(loginDataJSON == null)
        {
            return false;
        }

        return fileManager.saveFile(true, Globals.FILENAME_LOGIN_INFO, loginDataJSON.toString());
    }

    private void reEncryptFile(String path, String oldPass, String newPass)
    {
        String rawData = fileManager.readFile(true, path);
        rawData = PasswordEncryptionService.decrypt(rawData, oldPass);
        rawData = PasswordEncryptionService.encrypt(rawData, newPass);
        fileManager.saveFile(true, path, rawData);
    }

    private boolean modifyRecoveryData(String answer, String newPass)
    {
        JsonWrapper.RecoveryData recoveryData = JsonWrapper.
                readRecoveryData(fileManager.readFile(true, Globals.FILENAME_RECOVERY_INFO));

        String encryptedPass = PasswordEncryptionService.encrypt(newPass, answer);
        JSONObject bundle = JsonWrapper.createRecoveryData(recoveryData.hash, recoveryData.salt,
                recoveryData.question, encryptedPass);

        return fileManager.saveFile(true, Globals.FILENAME_RECOVERY_INFO, bundle.toString());
    }
}
