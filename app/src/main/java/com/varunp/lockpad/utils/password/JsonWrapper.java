package com.varunp.lockpad.utils.password;

import android.util.Base64;
import android.util.Log;

import com.varunp.lockpad.utils.Globals;

import org.json.JSONObject;

/**
 * Created by Varun on 1/15/2016.
 */
public class JsonWrapper
{
    public static final int LOGIN_DATA_PASSWORD = 0, LOGIN_DATA_SALT = 1;
    public static byte[][] readLoginData(String contents)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(contents);

            byte[] pass = Base64.decode(
                jsonObject.getString(Globals.JSON_LOGIN_PASSWORD_OBJECT), Base64.DEFAULT);
            byte[] salt = Base64.decode(
                    jsonObject.getString(Globals.JSON_LOGIN_SALT_OBJECT), Base64.DEFAULT);
            byte[] key = Base64.decode(
                    jsonObject.getString(Globals.JSON_LOGIN_KEY_OBJECT), Base64.DEFAULT);

            return new byte[][]{pass, salt, key};
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static JSONObject createLoginData(byte[] pass, byte[] s, byte[] enc)
    {
        JSONObject ret = new JSONObject();
        try
        {
            String password = Base64.encodeToString(pass, Base64.DEFAULT);
            String salt = Base64.encodeToString(s, Base64.DEFAULT);
            String key = Base64.encodeToString(enc, Base64.DEFAULT);

            ret.put(Globals.JSON_LOGIN_PASSWORD_OBJECT, password);
            ret.put(Globals.JSON_LOGIN_SALT_OBJECT, salt);
            ret.put(Globals.JSON_LOGIN_KEY_OBJECT, key);
        }
        catch (Exception e)
        {
            return null;
        }

        return ret;
    }

    public static JSONObject createRecoveryData(byte[] hash, byte[] salt, String question, byte[] key)
    {
        JSONObject data = new JSONObject();
        try
        {
            String answerHash = Base64.encodeToString(hash, Base64.DEFAULT);
            String answerSalt = Base64.encodeToString(salt, Base64.DEFAULT);
            String password = Base64.encodeToString(key, Base64.DEFAULT);

            data.put(Globals.JSON_RECOVERY_ANSWER_HASH_OBJECT, answerHash);
            data.put(Globals.JSON_RECOVERY_ANSWER_SALT_OBJECT, answerSalt);
            data.put(Globals.JSON_RECOVERY_PASSWORD_OBJECT, password);
            data.put(Globals.JSON_RECOVERY_QUESTION_OBJECT, question);

            return data;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static final class RecoveryData
    {
        public byte[] hash, salt;
        public String question, password;

        public RecoveryData(byte[] hash, byte[] salt, String question, String password)
        {
            this.hash = hash;
            this.salt = salt;
            this.question = question;
            this.password = password;
        }
    }

    public static RecoveryData readRecoveryData(String rawJSON)
    {
        try
        {
            JSONObject data = new JSONObject(rawJSON);

            byte[] hash = Base64.decode(
                    data.getString(Globals.JSON_RECOVERY_ANSWER_HASH_OBJECT), Base64.DEFAULT);
            byte[] salt = Base64.decode(
                    data.getString(Globals.JSON_RECOVERY_ANSWER_SALT_OBJECT), Base64.DEFAULT);

            String question = data.getString(Globals.JSON_RECOVERY_QUESTION_OBJECT);
            String password = data.getString(Globals.JSON_RECOVERY_PASSWORD_OBJECT);

            return new RecoveryData(hash, salt, question, password);
        }
        catch (Exception e)
        {
            Log.d("JsonWrapper", e.getMessage());
            return null;
        }
    }
}
