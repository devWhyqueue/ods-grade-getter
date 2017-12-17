package de.queisler.ods_grade_getter.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ProtectedConfigFile {

    public static void saveCredentials(String username, String password, Context ctx) throws
            Exception {
        String string = username + "#" + password;
        writeToFile(string, ctx);
    }

    public static String[] loadCredentials(Context ctx) throws Exception {

        String cred = readFromFile(ctx);
        if (cred.isEmpty()) return null;

        String[] credArr = new String[2];
        credArr[0] = cred.substring(0, cred.indexOf('#'));
        credArr[1] = cred.substring((cred.indexOf('#') + 1));

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
}
