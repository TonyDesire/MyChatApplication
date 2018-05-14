package ru.xyah.mychatapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewProfileActivity extends AppCompatActivity implements View.OnClickListener {

    final String Log_tag = "MyLogs";

    ImageView viewProfileActivity_photoImageView;
    TextView viewProfileActivity_firstNameTextView;
    TextView viewProfileActivity_middleNameTextView;
    TextView viewProfileActivity_secondNameTextView;
    TextView viewProfileActivity_ageTextView;
    TextView viewProfileActivity_genderTextView;
    TextView viewProfileActivity_phoneTextView;
    TextView viewProfileActivity_nationalityTextView;
    TextView viewProfileActivity_stateTextView;
    TextView viewProfileActivity_cityTextView;
    TextView viewProfileActivity_addressTextView;
    TextView viewProfileActivity_emailTextView;
    TextView viewProfileActivity_facultyTextView;

    Button viewProfileActivity_messageButton;
    Button viewProfileActivity_addToFriendButton;

    WorkingWithServerClass workingWithServerClass = new WorkingWithServerClass(this);

    DatabaseHelper databaseHelper;
    SQLiteDatabase dbLocal;
    Cursor cursor;

    String userPhotoName;

    File photosFolder;
    BitmapFactory.Options options;

    Intent receivedIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        photosFolder = new File(getCacheDir() + File.separator + "pictures");
        options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        workingWithServerClass.setLoginedAccountId(getIntent().getIntExtra("loginedAccountId", -1));

        viewProfileActivity_photoImageView = (ImageView) findViewById(R.id.viewProfileActivity_photoImageView);
        viewProfileActivity_firstNameTextView = (TextView) findViewById(R.id.viewProfileActivity_firstNameTextView);
        viewProfileActivity_middleNameTextView = (TextView) findViewById(R.id.viewProfileActivity_middleNameTextView);
        viewProfileActivity_secondNameTextView = (TextView) findViewById(R.id.viewProfileActivity_secondNameTextView);
        viewProfileActivity_ageTextView = (TextView) findViewById(R.id.viewProfileActivity_ageTextView);
        viewProfileActivity_genderTextView = (TextView) findViewById(R.id.viewProfileActivity_genderTextView);
        viewProfileActivity_phoneTextView = (TextView) findViewById(R.id.viewProfileActivity_phoneTextView);
        viewProfileActivity_nationalityTextView = (TextView) findViewById(R.id.viewProfileActivity_nationalityTextView);
        viewProfileActivity_stateTextView = (TextView) findViewById(R.id.viewProfileActivity_stateTextView);
        viewProfileActivity_cityTextView = (TextView) findViewById(R.id.viewProfileActivity_cityTextView);
        viewProfileActivity_addressTextView = (TextView) findViewById(R.id.viewProfileActivity_addressTextView);
        viewProfileActivity_emailTextView = (TextView) findViewById(R.id.viewProfileActivity_emailTextView);
        viewProfileActivity_facultyTextView = (TextView) findViewById(R.id.viewProfileActivity_facultyTextView);


        viewProfileActivity_messageButton = (Button) findViewById(R.id.viewProfileActivity_messageButton);
        viewProfileActivity_addToFriendButton = (Button) findViewById(R.id.viewProfileActivity_addToFriendButton);

        receivedIntent = getIntent();

        databaseHelper = DatabaseHelper.getInstance(this);
        dbLocal = databaseHelper.getWritableDatabase();

        if (receivedIntent.getBooleanExtra("ShowMyProfile", true) || (workingWithServerClass.getLoginedAccountId() == receivedIntent.getIntExtra("opennedProfileId", -1))) {
            viewProfileActivity_messageButton.setVisibility(View.GONE);
            viewProfileActivity_addToFriendButton.setVisibility(View.GONE);
            if (workingWithServerClass.getLoginedAccountId() != -1) {
                Cursor cursor = dbLocal.rawQuery("SELECT * FROM `Users` WHERE _id=" + workingWithServerClass.getLoginedAccountId(), null);
                cursor.moveToFirst();

                setUserPhoto(cursor.getString(cursor.getColumnIndex("photo_path")));
                viewProfileActivity_firstNameTextView.setText(cursor.getString(cursor.getColumnIndex("first_name")));
                viewProfileActivity_middleNameTextView.setText(cursor.getString(cursor.getColumnIndex("middle_name")));
                viewProfileActivity_secondNameTextView.setText(cursor.getString(cursor.getColumnIndex("second_name")));
                viewProfileActivity_ageTextView.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("age"))));
                viewProfileActivity_genderTextView.setText(cursor.getString(cursor.getColumnIndex("gender")));
                viewProfileActivity_phoneTextView.setText(cursor.getString(cursor.getColumnIndex("phone_number")));
                viewProfileActivity_nationalityTextView.setText(cursor.getString(cursor.getColumnIndex("nationality")));
                viewProfileActivity_stateTextView.setText(cursor.getString(cursor.getColumnIndex("state")));
                viewProfileActivity_cityTextView.setText(cursor.getString(cursor.getColumnIndex("city")));
                viewProfileActivity_addressTextView.setText(cursor.getString(cursor.getColumnIndex("address")));
                viewProfileActivity_emailTextView.setText(cursor.getString(cursor.getColumnIndex("email")));
                viewProfileActivity_facultyTextView.setText(cursor.getString(cursor.getColumnIndex("faculty")));
                cursor.close();

            }
        } else {
            Cursor cursor = dbLocal.rawQuery("SELECT * FROM `Users` WHERE _id=" + String.valueOf(receivedIntent.getIntExtra("opennedProfileId", -1)), null);
            cursor.moveToFirst();

            setUserPhoto(cursor.getString(cursor.getColumnIndex("photo_path")));
            viewProfileActivity_firstNameTextView.setText(cursor.getString(cursor.getColumnIndex("first_name")));
            viewProfileActivity_middleNameTextView.setText(cursor.getString(cursor.getColumnIndex("middle_name")));
            viewProfileActivity_secondNameTextView.setText(cursor.getString(cursor.getColumnIndex("second_name")));
            viewProfileActivity_ageTextView.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("age"))));
            viewProfileActivity_genderTextView.setText(cursor.getString(cursor.getColumnIndex("gender")));
            viewProfileActivity_phoneTextView.setText(cursor.getString(cursor.getColumnIndex("phone_number")));
            viewProfileActivity_nationalityTextView.setText(cursor.getString(cursor.getColumnIndex("nationality")));
            viewProfileActivity_stateTextView.setText(cursor.getString(cursor.getColumnIndex("state")));
            viewProfileActivity_cityTextView.setText(cursor.getString(cursor.getColumnIndex("city")));
            viewProfileActivity_addressTextView.setText(cursor.getString(cursor.getColumnIndex("address")));
            viewProfileActivity_emailTextView.setText(cursor.getString(cursor.getColumnIndex("email")));
            viewProfileActivity_facultyTextView.setText(cursor.getString(cursor.getColumnIndex("faculty")));
            cursor.close();
        }
        dbLocal.close();

        viewProfileActivity_messageButton.setOnClickListener(this);
        viewProfileActivity_addToFriendButton.setOnClickListener(this);
    }

    private void setUserPhoto(String userPhotoName) {
        if (!userPhotoName.equals("")) {
            File photoFile = new File(photosFolder + File.separator + userPhotoName);
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath(), options);
                viewProfileActivity_photoImageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewProfileActivity_messageButton:
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("author_id", workingWithServerClass.getLoginedAccountId());
                intent.putExtra("client_id", receivedIntent.getIntExtra("opennedProfileId", -1));
                intent.putExtra("loginedAccountId", workingWithServerClass.getLoginedAccountId());
                startActivity(intent);
                break;
            case R.id.viewProfileActivity_addToFriendButton:

                break;
        }
    }
}
