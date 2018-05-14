package ru.xyah.mychatapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    View mainMenuActivity_MyProfileButton;
    View mainMenuActivity_ShowMembersButton;
    View mainMenuActivity_MessagesButton;
    View mainMenuActivity_LogOutButton;
    View mainMenuActivity_ExitButton;

    WorkingWithServerClass workingWithServerClass = new WorkingWithServerClass(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        workingWithServerClass.setLoginedAccountId(getIntent().getIntExtra("loginedAccountId", -1));

        mainMenuActivity_MyProfileButton = (View) findViewById(R.id.mainMenuActivity_MyProfileButton);
        mainMenuActivity_ShowMembersButton = (View) findViewById(R.id.mainMenuActivity_ShowMembersButton);
        mainMenuActivity_MessagesButton = (View) findViewById(R.id.mainMenuActivity_MessagesButton);
        mainMenuActivity_LogOutButton = (View) findViewById(R.id.mainMenuActivity_LogOutButton);
        mainMenuActivity_ExitButton = (View) findViewById(R.id.mainMenuActivity_ExitButton);

        mainMenuActivity_MyProfileButton.setOnClickListener(this);
        mainMenuActivity_ShowMembersButton.setOnClickListener(this);
        mainMenuActivity_MessagesButton.setOnClickListener(this);
        mainMenuActivity_LogOutButton.setOnClickListener(this);
        mainMenuActivity_ExitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainMenuActivity_MyProfileButton:
                Intent intent2 = new Intent(this, ViewProfileActivity.class);
                intent2.putExtra("ShowMyProfile", true);
                intent2.putExtra("loginedAccountId", workingWithServerClass.getLoginedAccountId());
                startActivity(intent2);
                break;
            case R.id.mainMenuActivity_ShowMembersButton:
                Intent intent3 = new Intent(this, ShowMembersActivity.class);
                intent3.putExtra("loginedAccountId", workingWithServerClass.getLoginedAccountId());
                startActivity(intent3);
                break;
            case R.id.mainMenuActivity_MessagesButton:
                Intent intent4 = new Intent(this, MessagessActivity.class);
                intent4.putExtra("loginedAccountId", workingWithServerClass.getLoginedAccountId());
                startActivity(intent4);
                break;
            case R.id.mainMenuActivity_LogOutButton:
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
                databaseHelper.clearClientLogInDataTable();
                stopService(new Intent(this, UpdateDataService.class));
                Intent intent = new Intent(this, LogInActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.mainMenuActivity_ExitButton:
                stopService(new Intent(this, UpdateDataService.class));
                finish();
                break;
        }
    }
}
