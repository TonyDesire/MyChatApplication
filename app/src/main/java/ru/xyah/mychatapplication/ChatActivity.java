package ru.xyah.mychatapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, WorkingWithServerClass.Callback {

    int author_id;
    int client_id;

    String author_name;
    String client_name;

    SQLiteDatabase dbLocal;
    DatabaseHelper databaseHelper;
    WorkingWithServerClass workingWithServerClass;

    UpdateReceiver upd_res;

    ListView chatActivity_listView;
    EditText chatActivity_messageEditText;
    Button chatActivity_sendMessageBtn;

    ArrayList<HashMap<String, Object>> mList;
    MySimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        databaseHelper = DatabaseHelper.getInstance(this);

        workingWithServerClass = new WorkingWithServerClass(this);

        chatActivity_listView = (ListView) findViewById(R.id.chatActivity_listView);
        chatActivity_messageEditText = (EditText) findViewById(R.id.chatActivity_messageEditText);
        chatActivity_sendMessageBtn = (Button) findViewById(R.id.chatActivity_sendMessageBtn);

        author_id = getIntent().getIntExtra("author_id", -1);
        client_id = getIntent().getIntExtra("client_id", -1);

        dbLocal = databaseHelper.getReadableDatabase();
        Cursor cursor = dbLocal.rawQuery("SELECT * FROM Users WHERE _id=" + String.valueOf(author_id), null);
        cursor.moveToFirst();
        author_name = cursor.getString(cursor.getColumnIndex("first_name")) + " " + cursor.getString(cursor.getColumnIndex("middle_name")) + " " + cursor.getString(cursor.getColumnIndex("second_name"));
        cursor = dbLocal.rawQuery("SELECT * FROM Users WHERE _id=" + String.valueOf(client_id), null);
        cursor.moveToFirst();
        client_name = cursor.getString(cursor.getColumnIndex("first_name")) + " " + cursor.getString(cursor.getColumnIndex("middle_name")) + " " + cursor.getString(cursor.getColumnIndex("second_name"));
        cursor.close();
        dbLocal.close();

        getSupportActionBar().setTitle(client_name);

        workingWithServerClass.setLoginedAccountId(getIntent().getIntExtra("loginedAccountId", -1));
        workingWithServerClass.registerCallback(this);

        chatActivity_sendMessageBtn.setOnClickListener(this);

        upd_res = new UpdateReceiver();
        registerReceiver(upd_res, new IntentFilter("ru.xyah.mychatapplication.action.UPDATE_ListView"));

        mList = new ArrayList<HashMap<String, Object>>();
        adapter = new MySimpleAdapter(this, mList, R.layout.chat_activity_element_listview, null, null);
        chatActivity_listView.setAdapter(adapter);
        create_lv();
    }

    public void create_lv() {
        dbLocal = databaseHelper.getReadableDatabase();
        if (!dbLocal.isOpen()) return;
        Cursor cursor = dbLocal.rawQuery("SELECT * FROM Chat WHERE author_id = " + String.valueOf(author_id)
                + " OR author_id = " + String.valueOf(client_id) + " ORDER BY data", null);
        if (cursor.moveToFirst()) {
            // если в базе есть элементы соответствующие
            // нашим критериям отбора

            // создадим массив, создадим hashmap и заполним его результатом
            // cursor
            mList = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> hm;
            do {
                // мое сообщение !!!
                // если автор сообщения = автор
                // и получатель сообщения = клиент
                if (cursor.getInt(cursor.getColumnIndex("author_id")) == author_id && cursor.getInt(cursor.getColumnIndex("client_id")) == client_id) {
                    hm = new HashMap<>();
                    hm.put("author", author_name);
                    hm.put("client", "");
                    hm.put("list_client", "");
                    hm.put("list_client_time", "");
                    hm.put("list_author",
                            cursor.getString(cursor.getColumnIndex("text")));
                    hm.put("list_author_time", new SimpleDateFormat(
                            "HH:mm - dd.MM.yyyy").format(new Date(cursor.getLong(cursor.getColumnIndex("data")) * 1000)));
                    mList.add(hm);

                }

                // сообщение мне !!!!!!!
                // если автор сообщения = клиент
                // и если получатель сообщения = автор
                if (cursor.getInt(cursor.getColumnIndex("author_id")) == client_id && cursor.getInt(cursor.getColumnIndex("client_id")) == author_id) {
                    hm = new HashMap<>();
                    hm.put("author", "");
                    hm.put("client", client_name);
                    hm.put("list_author", "");
                    hm.put("list_author_time", "");
                    hm.put("list_client",
                            cursor.getString(cursor.getColumnIndex("text")));
                    hm.put("list_client_time", new SimpleDateFormat(
                            "HH:mm - dd.MM.yyyy").format(new Date(cursor
                            .getLong(cursor.getColumnIndex("data")) * 1000)));
                    mList.add(hm);
                }
            } while (cursor.moveToNext());
            cursor.close();
            dbLocal.close();
            // покажем lv
            adapter = new MySimpleAdapter(this, mList, R.layout.chat_activity_element_listview, null, null);
            chatActivity_listView.setAdapter(adapter);
        }
        Log.i("chat", "+ ChatActivity ======================== обновили поле чата");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chatActivity_sendMessageBtn:
                if (!chatActivity_messageEditText.getText().toString().equals("")) {
                    chatActivity_sendMessageBtn.setActivated(false);
                    String url = getString(R.string.server_name) +
                            "/chat.php?action=insert" +
                            "&author_id=" + String.valueOf(author_id) +
                            "&client_id=" + String.valueOf(client_id) +
                            "&text=" + chatActivity_messageEditText.getText().toString().replace(" ", "%20");
                    workingWithServerClass.sendMessage(url);
                    HashMap<String, Object> hm = new HashMap<>();
                    hm.put("author", author_name);
                    hm.put("list_author", chatActivity_messageEditText.getText().toString());

                    mList.add(hm);
                    adapter.notifyDataSetChanged();
                    chatActivity_messageEditText.setText("");
                }
                break;
        }
    }

    public class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("chat", "+ ChatActivity - ресивер получил сообщение - обновим ListView");
            create_lv();
        }
    }

    @Override
    public void onBackPressed() {
        Log.i("chat", "+ ChatActivity - закрыт");
        unregisterReceiver(upd_res);
        finish();
    }

    @Override
    public void INSERTtoChatSuccessfully() {
        sendBroadcast(new Intent("ru.xyah.mychatapplication.action.update_messages_table"));
        chatActivity_messageEditText.setText("");
        chatActivity_sendMessageBtn.setActivated(true);
    }

    @Override
    public void INSERTtoChatError() {
        Toast.makeText(this, "Error send", Toast.LENGTH_SHORT).show();
        chatActivity_sendMessageBtn.setActivated(false);
    }


    private class MySimpleAdapter extends SimpleAdapter{
        Context context;
        ArrayList<HashMap<String, Object>> mList;
        LayoutInflater lInflater;
        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         */
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            mList = (ArrayList<HashMap<String, Object>>) data;
            this.context = context;
            lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public HashMap<String, Object> getItemInfo(int position) {
            return mList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View chatElementView = inflater.inflate(R.layout.chat_activity_element_listview, parent, false);
            HashMap<String, Object> elem = mList.get(position);
            if (elem.get("author") != ""){
                chatElementView.findViewById(R.id.chatActivity_elementListView_clientMessageView).setVisibility(View.GONE);
                ((TextView)chatElementView.findViewById(R.id.author)).setText((String) elem.get("author"));
                ((TextView)chatElementView.findViewById(R.id.list_author)).setText((String) elem.get("list_author"));
                ((TextView)chatElementView.findViewById(R.id.list_author_time)).setText((String) elem.get("list_author_time"));
            } else {
                chatElementView.findViewById(R.id.chatActivity_elementListView_authorMessageView).setVisibility(View.GONE);
                ((TextView)chatElementView.findViewById(R.id.client)).setText((String) elem.get("client"));
                ((TextView)chatElementView.findViewById(R.id.list_client)).setText((String) elem.get("list_client"));
                ((TextView)chatElementView.findViewById(R.id.list_client_time)).setText((String) elem.get("list_client_time"));
            }
            return chatElementView;
        }
    }

    @Override
    public void logInIncorrectLoginData() {

    }

    @Override
    public void logInCorrectLoginData(int loginedUserId) {

    }

    @Override
    public void createAccountSuccessfullyCreated() {

    }

    @Override
    public void createAccountAlreadyExists() {

    }

    @Override
    public void createAccountNoConnDb() {

    }
}
