package net.dealforest.sample.crypt;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256Cipher {

    public static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
			throws java.io.UnsupportedEncodingException, 
				NoSuchAlgorithmException,
				NoSuchPaddingException,
				InvalidKeyException,
				InvalidAlgorithmParameterException,
				IllegalBlockSizeException,
				BadPaddingException {
		
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
    	SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
		return cipher.doFinal(textBytes);
	}

	public static byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes) 
			throws java.io.UnsupportedEncodingException, 
			NoSuchAlgorithmException,
			NoSuchPaddingException,
			InvalidKeyException,
			InvalidAlgorithmParameterException,
			IllegalBlockSizeException,
			BadPaddingException {
		
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
		SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
		return cipher.doFinal(textBytes);
	}

    public static final String DecryptionFailureErrorMessage = "Decryption Failure";
    public static final String EncryptionFailureErrorMessage = "Encryption Failure";

    public static final byte[] IvBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


    public static byte[] getKey() {
        return key;
    }

    public static void setKey(byte[] key) {
        AES256Cipher.key = key;
    }

    private static byte[] key;

    public static byte[] generateKey(String password)
    {
        try
        {
            byte[] key = (password).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 32); // use only first 128 bit

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            return key;
        }
        catch (Exception e)
        {
            Log.d("AES", e.getMessage());
            return null;
        }
    }

    public static String encrypt(String raw, byte[] key)
    {
        try
        {
            byte[] CipherData = AES256Cipher.encrypt(IvBytes, key, raw.getBytes("UTF-8"));
            return Base64.encodeToString(CipherData, Base64.DEFAULT);
        }
        catch (Exception e)
        {
            Log.d("AES256", e.getMessage());
            return EncryptionFailureErrorMessage;
        }
    }

    public static String decrypt(String raw, byte[] key)
    {
        try
        {
            byte[] CipherData = AES256Cipher.decrypt(IvBytes, key,
                    Base64.decode(raw.getBytes("UTF-8"), Base64.DEFAULT));
            return new String(CipherData, "UTF-8");
        }
        catch (Exception e)
        {
            Log.d("AES256", e.getMessage());
            return DecryptionFailureErrorMessage;
        }
    }
}
