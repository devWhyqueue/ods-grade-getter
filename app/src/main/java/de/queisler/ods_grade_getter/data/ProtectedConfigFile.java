package de.queisler.ods_grade_getter.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class ProtectedConfigFile {

    public static void saveCredentials(String username, String password, Context ctx) throws
            GeneralSecurityException, IOException {
        // The salt (probably) can be stored along with the encrypted data
        byte[] salt = new String("12345678").getBytes();

        // Decreasing this speeds down startup time and can be useful during testing, but it also
        // makes it easier for brute force attackers
        int iterationCount = 40000;
        // Other values give me java.security.InvalidKeyException: Illegal key size or default
        // parameters
        int keyLength = 128;
        SecretKeySpec key = createSecretKey("thisIsSafeISwear".toCharArray(), salt,
                iterationCount, keyLength);

        String encryptedPassword = encrypt(password, key);

        String filename = "cred";
        String string = username + "#" + encryptedPassword;
        writeToFile(string, ctx);
    }

    public static String[] loadCredentials(Context ctx) throws GeneralSecurityException,
            IOException {
        // The salt (probably) can be stored along with the encrypted data
        byte[] salt = new String("12345678").getBytes();

        // Decreasing this speeds down startup time and can be useful during testing, but it also
        // makes it easier for brute force attackers
        int iterationCount = 40000;
        // Other values give me java.security.InvalidKeyException: Illegal key size or default
        // parameters
        int keyLength = 128;
        SecretKeySpec key = createSecretKey("thisIsSafeISwear".toCharArray(), salt,
                iterationCount, keyLength);

        String cred = readFromFile(ctx);
        String[] credArr = new String[2];
        credArr[0] = cred.substring(0, cred.indexOf('#'));
        System.out.println(credArr[0]);
        credArr[1] = cred.substring((cred.indexOf('#') + 1));
        credArr[1] = decrypt(credArr[1], key);
        System.out.println(credArr[1]);

        return credArr;
    }

    private static void writeToFile(String data, Context context) {
        try {
            context.deleteFile("config.txt");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput
                    ("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int
            iterationCount, int keyLength) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    private static String encrypt(String property, SecretKeySpec key) throws
            GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(property.getBytes("UTF-8"));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String decrypt(String string, SecretKeySpec key) throws
            GeneralSecurityException, IOException {
        String iv = string.split(":")[0];
        String property = string.split(":")[1];
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }
}
