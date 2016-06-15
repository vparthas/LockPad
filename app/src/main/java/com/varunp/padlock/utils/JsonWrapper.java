package com.varunp.padlock.utils;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Varun on 1/15/2016.
 */
public class JsonWrapper
{
    public static byte[][] readLoginData(String contents)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(contents);

            byte[] pass = Base64.decode(
                jsonObject.getString(Globals.JSON_LOGIN_PASSWORD_OBJECT), Base64.DEFAULT);
            byte[] salt = Base64.decode(
                    jsonObject.getString(Globals.JSON_LOGIN_SALT_OBJECT), Base64.DEFAULT);

            return new byte[][]{pass, salt};
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static JSONObject createLoginData(byte[] pass, byte[] s)
    {
        JSONObject ret = new JSONObject();
        try
        {
            String password = Base64.encodeToString(pass, Base64.DEFAULT);
            String salt = Base64.encodeToString(s, Base64.DEFAULT);

            ret.put(Globals.JSON_LOGIN_PASSWORD_OBJECT, password);
            ret.put(Globals.JSON_LOGIN_SALT_OBJECT, salt);
        }
        catch (Exception e)
        {
            return null;
        }

        return ret;
    }

    public static JSONObject createRecoveryData(byte[] hash, byte[] salt, String question, String password)
    {
        JSONObject data = new JSONObject();
        try
        {
            String answerHash = Base64.encodeToString(hash, Base64.DEFAULT);
            String answerSalt = Base64.encodeToString(salt, Base64.DEFAULT);

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
}
