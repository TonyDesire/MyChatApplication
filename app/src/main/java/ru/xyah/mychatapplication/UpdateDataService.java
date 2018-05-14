package ru.xyah.mychatapplication;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by XYAH on 11.04.17.
 */

public class UpdateDataService extends Service {

    checkUsersThread checkUsersThread;
    checkChatTableThread checkChatTableThread;

    String server_name = "http://j33306kb.beget.tech";


    int loginedAccountId = -1;
    final String Log_tag = "MyLogs";

    DatabaseHelper databaseHelper;

    File photosFolder;

    @Override
    public void onCreate() {
        databaseHelper = DatabaseHelper.getInstance(this);
        Log.d(Log_tag, "Service created");
        photosFolder = new File(getCacheDir() + File.separator + "pictures");
        if (!photosFolder.exists()) {
            photosFolder.mkdir();
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loginedAccountId = intent.getIntExtra("loginedAccountId", -1);
        Log.d(Log_tag, "loginedAccountId is " + String.valueOf(loginedAccountId));
        run();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(Log_tag, "Service destroyed");
        if (checkUsersThread != null) checkUsersThread.cancel(false);
        if (checkChatTableThread != null) checkChatTableThread.cancel(false);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void run() {
        checkUsersTable();
        checkChatTable();
    }

    public void checkUsersTable() {
        checkUsersThread = new checkUsersThread();
        checkUsersThread.execute();
    }

    public void checkChatTable() {
        checkChatTableThread = new checkChatTableThread();
        checkChatTableThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class checkUsersThread extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                if (isCancelled()) {
                    Log.d(Log_tag, "Service is canceled");
                    break;
                }
                try {
                    HttpURLConnection conn = null;
                    String url = server_name + "/loadUsers.php";
                    try {
                        Log.i("chat",
                                "+ FoneService --------------- ОТКРОЕМ СОЕДИНЕНИЕ");

                        conn = (HttpURLConnection) new URL(url).openConnection();
                        conn.setReadTimeout(10000);
                        conn.setConnectTimeout(15000);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        conn.setDoInput(true);
                        conn.connect();

                    } catch (Exception e) {
                        Log.i("chat", "+ FoneService ошибка: " + e.getMessage());
                    }
                    // получаем ответ ---------------------------------->
                    try {
                        if (conn != null) {
                            InputStream is = conn.getInputStream();
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(is, "UTF-8"));
                            StringBuilder sb = new StringBuilder();
                            String bfr_st = null;
                            while ((bfr_st = br.readLine()) != null) {
                                sb.append(bfr_st);
                            }
                            Log.i("chat", "+ FoneService - полный ответ сервера:\n" + sb.toString());
                            String answer = sb.toString();
                            is.close();
                            br.close();
                            publishProgress(answer);
                        } else throw new Exception();
                    } catch (Exception e) {
                        Log.i("chat", "+ FoneService ошибка: " + e.getMessage());
                    } finally {
                        conn.disconnect();
                        Log.i("chat", "+ FoneService close connection");
                    }
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(Log_tag, values[0]);
            try {
                ArrayList<String> photosNamesArrayList = new ArrayList<String>();
                JSONArray jsonArray = new JSONArray(values[0]);
                SQLiteDatabase dbLocal = databaseHelper.getWritableDatabase();
                dbLocal.execSQL("Delete From `Users`");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    ContentValues user = new ContentValues();
                    user.put("_id", jo.getInt("id"));
                    user.put("first_name", jo.getString("first_name"));
                    user.put("second_name", jo.getString("second_name"));
                    user.put("age", jo.getInt("age"));
                    user.put("photo_path", jo.getString("photo_path"));
                    user.put("middle_name", jo.getString("middle_name"));
                    user.put("nationality", jo.getString("nationality"));
                    user.put("state", jo.getString("state"));
                    user.put("city", jo.getString("city"));
                    user.put("address", jo.getString("address"));
                    user.put("email", jo.getString("email"));
                    user.put("gender", jo.getString("gender"));
                    user.put("faculty", jo.getString("faculty"));
                    user.put("phone_number", jo.getString("phone_number"));
                    dbLocal.insert("Users", null, user);
                    if (!jo.getString("photo_path").equals(""))
                        photosNamesArrayList.add(jo.getString("photo_path"));
                }
                dbLocal.close();
                sendBroadcast(new Intent("ru.xyah.mychatapplication.action.UPDATE_Users_ListView"));
                downloadPhotos(photosNamesArrayList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onProgressUpdate(values);
        }
    }

    class checkChatTableThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                if (isCancelled()) {
                    Log.d(Log_tag, "Service is canceled");
                    break;
                }
                String answer = "";
                HttpURLConnection conn = null;
                String lnk = getString(R.string.server_name) + "/chat.php?action=select&loginned_account_id=" + loginedAccountId;
                SQLiteDatabase dbLocal = databaseHelper.getWritableDatabase();
                Cursor cursor = dbLocal.rawQuery("SELECT * FROM Chat WHERE author_id=" + loginedAccountId + " OR client_id=" + loginedAccountId + " ORDER BY data", null);
                if (cursor.moveToLast()) {
                    long last_time = cursor.getLong(cursor.getColumnIndex("data"));
                    lnk = lnk + "&data=" + String.valueOf(last_time);
                }
                cursor.close();
                try {
                    Log.i("chat",
                            "+ FoneService --------------- ОТКРОЕМ СОЕДИНЕНИЕ");

                    conn = (HttpURLConnection) new URL(lnk)
                            .openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setDoInput(true);
                    conn.connect();

                } catch (Exception e) {
                    Log.i("chat", "+ FoneService ошибка: " + e.getMessage());
                }
                try {
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String bfr_st = null;
                    while ((bfr_st = br.readLine()) != null) {
                        sb.append(bfr_st);
                    }

                    Log.i("chat", "+ FoneService - полный ответ сервера:\n"
                            + sb.toString());
                    // сформируем ответ сервера в string
                    // обрежем в полученном ответе все, что находится за "]"
                    // это необходимо, т.к. json ответ приходит с мусором
                    // и если этот мусор не убрать - будет невалидным
                    answer = sb.toString();
                    answer = answer.substring(0, answer.indexOf("]") + 1);

                    is.close(); // закроем поток
                    br.close(); // закроем буфер
                } catch (Exception e) {
                    Log.i("chat", "+ FoneService ошибка: " + e.getMessage());
                } finally {
                    conn.disconnect();
                    Log.i("chat",
                            "+ FoneService --------------- ЗАКРОЕМ СОЕДИНЕНИЕ");
                }
                if (answer != null && !answer.trim().equals("")) {
                    Log.i("chat", "+ FoneService ---------- ответ содержит JSON:");
                    try {
                        // ответ превратим в JSON массив
                        JSONArray ja = new JSONArray(answer);
                        JSONObject jo;

                        Integer i = 0;
                        dbLocal = databaseHelper.getWritableDatabase();
                        while (i < ja.length()) {

                            // разберем JSON массив построчно
                            jo = ja.getJSONObject(i);

                            // создадим новое сообщение
                            ContentValues new_mess = new ContentValues();
                            new_mess.put("author_id", jo.getInt("author_id"));
                            new_mess.put("client_id", jo.getInt("client_id"));
                            new_mess.put("data", jo.getLong("data"));
                            new_mess.put("text", jo.getString("text"));
                            // запишем новое сообщение в БД
                            dbLocal.insert("Chat", null, new_mess);
                            new_mess.clear();

                            i++;

                            // отправим броадкаст для ChatActivity
                            // если она открыта - она обновить ListView
                        }
                        dbLocal.close();
                        sendBroadcast(new Intent("ru.xyah.mychatapplication.action.UPDATE_ListView"));
                    } catch (Exception e) {
                        // если ответ сервера не содержит валидный JSON
                        Log.i("chat",
                                "+ FoneService ---------- ошибка ответа сервера:\n"
                                        + e.getMessage());
                    }

                } else {
                    // если ответ сервера пустой
                    Log.i("chat",
                            "+ FoneService ---------- ответ не содержит JSON!");
                }

                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    Log.i("chat",
                            "+ FoneService - ошибка процесса: "
                                    + e.getMessage());
                }
            }
            return null;
        }
    }

    private void downloadPhotos(ArrayList<String> photos) {
        for (int i = 0; i < photos.size(); i++) {
            File photoFile = new File(photosFolder + File.separator + photos.get(i));
            if (!photoFile.exists())
                new downloadPhotosThread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, photoFile);
        }

    }

    class downloadPhotosThread extends AsyncTask<File, Void, Void> {

        @Override
        protected Void doInBackground(File... params) {
            File downloadedFile = params[0];
            Log.d("MyLogs", "Downloading file " + downloadedFile.getName());

            byte[] buffer = new byte[1024];
            String urlString = getString(R.string.server_name) + "/downloadFile.php?file_name=" + downloadedFile.getName();
            Log.d("MyLogs", urlString);
            try {
                URL url = new URL(urlString);
                InputStream in = new BufferedInputStream(url.openStream());
                OutputStream fout = new FileOutputStream(downloadedFile);

                byte data[] = new byte[1024];

                int count;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    fout.write(data, 0, count);
                }

                // flushing output
                fout.flush();

                // closing streams
                fout.close();
                in.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }
    }
}
