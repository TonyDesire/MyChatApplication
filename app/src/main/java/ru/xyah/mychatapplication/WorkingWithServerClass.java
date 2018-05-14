package ru.xyah.mychatapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Created by XYAH on 11.04.17.
 */

public class WorkingWithServerClass {
    Callback callback;
    private int loginedAccountId;
    Context context;

    WorkingWithServerClass(Context context) {
        this.context = context;
    }

    String server_name = "http://j33306kb.beget.tech";

    WorkingWithServerClass() {
        this.loginedAccountId = -1;
    }

    public void logIn(final Context context,String login, String password) {
        final String main_url = server_name + "/login.php?login=" + login + "&password=" + password;
        class logIn extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    Log.d("MyLogs", "Trying To connect with server to login");
                    URL url = new URL(main_url);
                    InputStream is = url.openConnection().getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    is.close();
                    reader.close();
                    return buffer.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                Log.d("MyLogs", s);
                if (s != null && s != "") {
                    try {
                        JSONArray jsonArray = new JSONArray(s);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        int result = jsonObject.getInt("user_id");
                        setLoginedAccountId(result);
                        callback.logInCorrectLoginData(result);
                        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
                        databaseHelper.clearClientLogInDataTable();
                        ContentValues LogInUserData = new ContentValues();
                        LogInUserData.put("_id", jsonObject.getInt("user_id"));
                        LogInUserData.put("login", jsonObject.getString("login"));
                        LogInUserData.put("password", jsonObject.getString("password"));
                        SQLiteDatabase dbLocal = databaseHelper.getWritableDatabase();
                        dbLocal.insert("ClientLogInData", null, LogInUserData);
                        dbLocal.close();
                    } catch (JSONException e) {
                        callback.logInIncorrectLoginData();
                    }
                } else {
                    callback.logInIncorrectLoginData();
                }
            }
        }
        new logIn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void createUser(String login, String password, String firstName, String secondName, String middleName, int age, String gender, String phoneNumber, String nationality, String state, String city, String address, String email, String faculty, final Uri photoPath) {
        String main_url = "";
        if (photoPath != null) {
            String namePhoto = login + "_" + String.valueOf(new Timestamp(System.currentTimeMillis() / 1000).getTime()) + "." + getFileExtension(photoPath);
            main_url = server_name + "/createUser.php?login=" + login + "&password=" + password
                    + "&first_name=" + firstName + "&second_name=" + secondName
                    + "&age=" + String.valueOf(age) + "&middle_name=" + middleName
                    + "&gender=" + gender + "&nationality=" + nationality + "&state=" + state
                    + "&city=" + city + "&address=" + address + "&email=" + email
                    + "&faculty=" + faculty + "&phone_number=" + phoneNumber
                    + "&photo_path=" + namePhoto;
            uploadPhoto(photoPath, namePhoto);
        } else {
            main_url = server_name + "/createUser.php?login=" + login + "&password=" + password
                    + "&first_name=" + firstName + "&second_name=" + secondName
                    + "&age=" + String.valueOf(age) + "&middle_name=" + middleName
                    + "&gender=" + gender + "&nationality=" + nationality + "&state=" + state
                    + "&city=" + city + "&address=" + address + "&email=" + email
                    + "&faculty=" + faculty + "&phone_number=" + phoneNumber;
        }
        class createUser extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Log.d("MyLogs", params[0]);
                    URL url = new URL(params[0]);
                    InputStream is = url.openConnection().getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    is.close();
                    reader.close();
                    return buffer.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                Log.d("MyLogs", s);
                switch (s.toString().toLowerCase().trim()) {
                    case "ok":
                        callback.createAccountSuccessfullyCreated();
                        break;
                    case "already_exists":
                        callback.createAccountAlreadyExists();
                        break;
                    case "no_conn_db":
                        callback.createAccountNoConnDb();
                        break;
                }
                super.onPostExecute(s);
            }
        }
        new createUser().execute(main_url);
    }

    public void uploadPhoto(final Uri filePath, final String filename) {
        Log.d("MyLogs", filename);
        class UploadFileAsync extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String sourceFileUri = filePath.getPath();
                    HttpURLConnection conn = null;
                    DataOutputStream dos = null;
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";
                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBufferSize = 1 * 1024 * 1024;
                    File sourceFile = new File(context.getCacheDir() + "tmp");
                    sourceFile.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(sourceFile);
                    ImageCompressor.decodeSampledBitmapFromFile(sourceFileUri, 512, 512).compress(Bitmap.CompressFormat.PNG, 0, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    //File sourceFile = new File(sourceFileUri);
                    Log.d("MyLogs", sourceFile.getAbsolutePath() + " " + sourceFile.isFile() + " ");
                    if (sourceFile.isFile()) {
                        try {
                            String upLoadServerUri = "http://j33306kb.beget.tech/uploadFile.php?";
                            // open a URL connection to the Servlet
                            FileInputStream fileInputStream = new FileInputStream(
                                    sourceFile);
                            URL url = new URL(upLoadServerUri);

                            // Open a HTTP connection to the URL
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setDoInput(true); // Allow Inputs
                            conn.setDoOutput(true); // Allow Outputs
                            conn.setUseCaches(false); // Don't use a Cached Copy
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Connection", "Keep-Alive");
                            conn.setRequestProperty("ENCTYPE",
                                    "multipart/form-data");
                            conn.setRequestProperty("Content-Type",
                                    "multipart/form-data;boundary=" + boundary);
                            conn.setRequestProperty("bill", sourceFileUri);

                            dos = new DataOutputStream(conn.getOutputStream());

                            dos.writeBytes(twoHyphens + boundary + lineEnd);
                            dos.writeBytes("Content-Disposition: form-data; name=\"bill\";filename=\"" + filename + "\"" + lineEnd);

                            dos.writeBytes(lineEnd);

                            // create a buffer of maximum size
                            bytesAvailable = fileInputStream.available();

                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            buffer = new byte[bufferSize];

                            // read file and write it into form...
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                            while (bytesRead > 0) {

                                dos.write(buffer, 0, bufferSize);
                                bytesAvailable = fileInputStream.available();
                                bufferSize = Math
                                        .min(bytesAvailable, maxBufferSize);
                                bytesRead = fileInputStream.read(buffer, 0,
                                        bufferSize);

                            }

                            // send multipart form data necesssary after file
                            // data...
                            dos.writeBytes(lineEnd);
                            dos.writeBytes(twoHyphens + boundary + twoHyphens
                                    + lineEnd);

                            // Responses from the server (code and message)
                            int serverResponseCode = conn.getResponseCode();
                            String serverResponseMessage = conn
                                    .getResponseMessage();

                            if (serverResponseCode == 200) {
                                Log.d("MyLogs", "File Upload Complete.");
                                // messageText.setText(msg);
                                //Toast.makeText(ctx, "File Upload Complete.",
                                //      Toast.LENGTH_SHORT).show();

                                // recursiveDelete(mDirectory1);

                            }

                            // close the streams //
                            fileInputStream.close();
                            dos.flush();
                            dos.close();

                        } catch (Exception e) {

                            // dialog.dismiss();
                            e.printStackTrace();

                        }
                        // dialog.dismiss();

                    } // End else block


                } catch (Exception ex) {
                    // dialog.dismiss();

                    ex.printStackTrace();
                }
                return "Executed";
            }

            @Override
            protected void onPostExecute(String result) {

            }

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected void onProgressUpdate(Void... values) {
            }
        }


        new UploadFileAsync().execute();
    }

    public void sendMessage(String url) {
        class insertToChat extends AsyncTask<String, Void, Integer> {
            HttpURLConnection conn;
            Integer res;
            protected Integer doInBackground(String... params) {
                Log.d("MyLogs", params[0]);
                try {
                    String post_url = params[0];
                    Log.d("MyLogs", post_url);
                    // соберем линк для передачи новой строки
                    Log.i("chat",
                            "+ ChatActivity - отправляем на сервер новое сообщение: "
                                    + post_url);

                    URL url = new URL(post_url);
                    Log.d("MyLogs", url.getHost());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10000); // ждем 10сек
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.connect();

                    res = conn.getResponseCode();
                    Log.i("chat", "+ ChatActivity - ответ сервера (200 - все ОК): "
                            + res.toString());

                } catch (Exception e) {
                    Log.i("chat",
                            "+ ChatActivity - ошибка соединения: " + e.getMessage());

                } finally {
                    // закроем соединение
                    conn.disconnect();
                }
                return res;
            }

            protected void onPostExecute(Integer result) {
                try {
                    if (result == 200) {
                        Log.i("chat", "+ ChatActivity - сообщение успешно ушло.");
                        callback.INSERTtoChatSuccessfully();
                    }
                } catch (Exception e) {
                    callback.INSERTtoChatError();
                    Log.i("chat", "+ ChatActivity - ошибка передачи сообщения:\n"
                            + e.getMessage());
                }
            }
        }
        //new insertToChat().execute(url);
        new insertToChat().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    interface Callback {
        void logInIncorrectLoginData();
        void logInCorrectLoginData(int loginedUserId);

        void createAccountSuccessfullyCreated();
        void createAccountAlreadyExists();
        void createAccountNoConnDb();

        void INSERTtoChatSuccessfully();
        void INSERTtoChatError();
    }

    public void registerCallback(Callback callback) {
        this.callback = callback;
    }


    private static String getFileExtension(Uri filePath) {
        String fileName = filePath.getPath();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    public int getLoginedAccountId() {
        return this.loginedAccountId;
    }

    public void setLoginedAccountId(int loginedAccountId1) {
        this.loginedAccountId = loginedAccountId1;
    }
}
