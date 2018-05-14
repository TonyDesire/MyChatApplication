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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ShowMembersActivity extends AppCompatActivity {

    ListView showMembersActivity_listView;

    WorkingWithServerClass workingWithServerClass = new WorkingWithServerClass(this);

    DatabaseHelper databaseHelper;
    SQLiteDatabase dbLocal;
    Cursor cursor;

    MySimpleCursonAdapter simpleCursorAdapter;

    File photosFolder;
    BitmapFactory.Options options;

    UpdateReceiver upd_res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_members);

        photosFolder = new File(getCacheDir() + File.separator + "pictures");
        options = new BitmapFactory.Options();
        options.inSampleSize = 10;

        databaseHelper = DatabaseHelper.getInstance(this);
        workingWithServerClass.setLoginedAccountId(getIntent().getIntExtra("loginedAccountId", -1));

        showMembersActivity_listView = (ListView) findViewById(R.id.showMembersActivity_listView);

        showMembersActivity_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(ShowMembersActivity.this, String.valueOf(id), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), ViewProfileActivity.class);
                intent.putExtra("ShowMyProfile", false);
                intent.putExtra("loginedAccountId", workingWithServerClass.getLoginedAccountId());
                intent.putExtra("opennedProfileId", (int) id);
                startActivity(intent);
            }
        });
        upd_res = new UpdateReceiver();
        registerReceiver(upd_res, new IntentFilter("ru.xyah.mychatapplication.action.UPDATE_Users_ListView"));
        create_lv();
    }

    public void create_lv(){
        dbLocal = databaseHelper.getWritableDatabase();
        if (!dbLocal.isOpen()) return;
        cursor = dbLocal.rawQuery("SELECT * FROM `Users`", null);

        String[] from = {"first_name", "middle_name", "second_name", "photo_path"};
        int[] to = {R.id.showMembersActivity_listView_element_firstNameTextView, R.id.showMembersActivity_listView_element_middleNameTextView, R.id.showMembersActivity_listView_element_secondNameTextView, R.id.showMembersActivity_listView_element_photoImageView};

        simpleCursorAdapter = new MySimpleCursonAdapter(this, R.layout.show_members_activity_element_listview, cursor, from, to, 0);
        showMembersActivity_listView.setAdapter(simpleCursorAdapter);
        dbLocal.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class MySimpleCursonAdapter extends SimpleCursorAdapter {


        public MySimpleCursonAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void setViewImage(ImageView v, String value) {
            Log.d("MyLogs", value);
            if (!value.equals("")) {
                File photoFile = new File(photosFolder + File.separator + value);
                if (photoFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath(), options);
                    v.setImageBitmap(bitmap);
                }
            } else v.setImageResource(R.drawable.no_image2);
        }
    }

    public class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("chat", "+ ShowMembers - ресивер получил сообщение - обновим ListView");
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
