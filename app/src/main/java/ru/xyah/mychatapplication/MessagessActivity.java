package ru.xyah.mychatapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessagessActivity extends AppCompatActivity {

    SQLiteDatabase dbLocal;
    DatabaseHelper databaseHelper;
    WorkingWithServerClass workingWithServerClass = new WorkingWithServerClass(this);

    ListView messagessActivity_listView;

    ArrayList<HashMap<String, Object>> mList;

    UpdateReceiver upd_res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messagess);

        messagessActivity_listView = (ListView) findViewById(R.id.messagessActivity_listView);

        databaseHelper = DatabaseHelper.getInstance(this);
        workingWithServerClass.setLoginedAccountId(getIntent().getIntExtra("loginedAccountId", -1));
        upd_res = new UpdateReceiver();
        registerReceiver(upd_res, new IntentFilter("ru.xyah.mychatapplication.action.UPDATE_ListView"));
        create_lv();
        messagessActivity_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra("author_id", workingWithServerClass.getLoginedAccountId());
                if ((int)mList.get((int) id).get("client_id") != workingWithServerClass.getLoginedAccountId())
                    intent.putExtra("client_id", (int) mList.get((int) id).get("client_id"));
                else
                    intent.putExtra("client_id", (int) mList.get((int) id).get("author_id"));
                intent.putExtra("loginedAccountId", workingWithServerClass.getLoginedAccountId());
                startActivity(intent);
                Log.d("MyLogs", mList.get((int) id).toString());
            }
        });
    }

    public void create_lv() {
        mList = new ArrayList<HashMap<String, Object>>();
        dbLocal = databaseHelper.getReadableDatabase();
        if (!dbLocal.isOpen()) return;
        Cursor cursor = dbLocal.rawQuery("SELECT * FROM Chat WHERE author_id = " + String.valueOf(workingWithServerClass.getLoginedAccountId())
                + " OR client_id = " + String.valueOf(workingWithServerClass.getLoginedAccountId()) + " ORDER BY data DESC", null);
        if (cursor.moveToFirst()) {
            HashMap<String, Object> hm;
            do {
                boolean founded = false;
                for (int i = 0; i < mList.size(); i++) {
                    if (((((int) mList.get(i).get("author_id")) == cursor.getInt(cursor.getColumnIndex("author_id"))) &&
                            ((int) mList.get(i).get("client_id")) == cursor.getInt(cursor.getColumnIndex("client_id"))) ||
                            ((((int) mList.get(i).get("author_id")) == cursor.getInt(cursor.getColumnIndex("client_id"))) &&
                                    ((int) mList.get(i).get("client_id")) == cursor.getInt(cursor.getColumnIndex("author_id")))) {
                        founded = true;
                        break;
                    }
                }
                if (!founded) {
                    hm = new HashMap<String, Object>();
                    hm.put("author_id", cursor.getInt(cursor.getColumnIndex("author_id")));
                    hm.put("client_id", cursor.getInt(cursor.getColumnIndex("client_id")));
                    hm.put("time", cursor.getLong(cursor.getColumnIndex("data")) * 1000);
                    hm.put("text", cursor.getString(cursor.getColumnIndex("text")));

                    mList.add(hm);
                }
            } while (cursor.moveToNext());
        }
        cursor = dbLocal.rawQuery("SELECT * FROM Users", null);
        if (cursor.moveToFirst()) {
            do {
                for (int i = 0; i < mList.size(); i++) {
                    if (cursor.getInt(cursor.getColumnIndex("_id")) == workingWithServerClass.getLoginedAccountId())
                        continue;
                    if ((cursor.getInt(cursor.getColumnIndex("_id")) == (int) mList.get(i).get("author_id")) ||
                            (cursor.getInt(cursor.getColumnIndex("_id")) == (int) mList.get(i).get("client_id"))) {
                        mList.get(i).put("client_name", cursor.getString(cursor.getColumnIndex("first_name")) +
                                " " +
                                cursor.getString(cursor.getColumnIndex("second_name")));
                        mList.get(i).put("client_photo", cursor.getString(cursor.getColumnIndex("photo_path")));
                        break;
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        dbLocal.close();
        MySimpleAdapter adapter = new MySimpleAdapter(this, mList, R.layout.messagess_activity_element_listview, null, null);

        messagessActivity_listView.setAdapter(adapter);
        for (int i = 0; i < mList.size(); i++) {
            Log.d("MyLogs", mList.get(i).toString());
        }
    }

    private class MySimpleAdapter extends SimpleAdapter {
        Context context;
        ArrayList<HashMap<String, Object>> mList;
        LayoutInflater lInflater;
        Bitmap authorPhoto;
        BitmapFactory.Options bitmapOptions;
        File photosFolder = new File(getCacheDir() + File.separator + "pictures");

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
            bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = 10;

            dbLocal = databaseHelper.getReadableDatabase();
            Cursor cursor = dbLocal.rawQuery("SELECT * FROM Users WHERE _id=" + String.valueOf(workingWithServerClass.getLoginedAccountId()), null);
            cursor.moveToFirst();
            if (!cursor.getString(cursor.getColumnIndex("photo_path")).equals(""))
                authorPhoto = BitmapFactory.decodeFile(photosFolder + File.separator + cursor.getString(cursor.getColumnIndex("photo_path")), bitmapOptions);
            cursor.close();
            dbLocal.close();

        }

        public HashMap<String, Object> getItemInfo(int position) {
            return mList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View messageElementView = inflater.inflate(R.layout.messagess_activity_element_listview, parent, false);
            HashMap<String, Object> elem = mList.get(position);
            if (!elem.get("client_photo").equals("")) {
                Bitmap clientPhoto = BitmapFactory.decodeFile(photosFolder + File.separator + elem.get("client_photo"), bitmapOptions);
                ((ImageView) messageElementView.findViewById(R.id.messagessActivity_listView_element_clientPhotoImageView)).setImageBitmap(clientPhoto);
            } else {
                ((ImageView) messageElementView.findViewById(R.id.messagessActivity_listView_element_clientPhotoImageView)).setImageResource(R.drawable.no_image2);
            }
            ((TextView) messageElementView.findViewById(R.id.messagessActivity_listView_element_clientNameTextView)).setText((String) elem.get("client_name"));
            ((TextView) messageElementView.findViewById(R.id.messagessActivity_listView_element_messageTextView)).setText((String) elem.get("text"));
            ((TextView) messageElementView.findViewById(R.id.messagessActivity_listView_element_timeTextView)).setText(new SimpleDateFormat("dd.MM HH:mm").format(new Date((long) elem.get("time"))));

            if ((int) elem.get("author_id") != workingWithServerClass.getLoginedAccountId())
                (messageElementView.findViewById(R.id.messagessActivity_listView_element_authorPhotoImageView)).setVisibility(View.GONE);
            else{
                if(authorPhoto != null)  ((ImageView) messageElementView.findViewById(R.id.messagessActivity_listView_element_authorPhotoImageView)).setImageBitmap(authorPhoto);
                else ((ImageView) messageElementView.findViewById(R.id.messagessActivity_listView_element_authorPhotoImageView)).setImageResource(R.drawable.no_image2);
            }


            return messageElementView;
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
}
