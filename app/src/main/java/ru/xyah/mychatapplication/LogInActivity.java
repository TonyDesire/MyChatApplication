package ru.xyah.mychatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogInActivity extends AppCompatActivity implements WorkingWithServerClass.Callback, View.OnClickListener {

    EditText logInActivity_LoginEditText;
    EditText logInActivity_PasswordEditText;
    Button logInActivity_LoginButton;
    Button logInActivity_CreateUserButton;

    WorkingWithServerClass workingWithServerClass = new WorkingWithServerClass(this);

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        workingWithServerClass.registerCallback(this);
        setContentView(R.layout.activity_log_in);

        logInActivity_LoginEditText = (EditText) findViewById(R.id.logInActivity_LoginEditText);
        logInActivity_PasswordEditText = (EditText) findViewById(R.id.logInActivity_PasswordEditText);
        logInActivity_LoginButton = (Button) findViewById(R.id.logInActivity_LoginButton);
        logInActivity_CreateUserButton = (Button) findViewById(R.id.logInActivity_CreateUserButton);

        logInActivity_LoginButton.setOnClickListener(this);
        logInActivity_CreateUserButton.setOnClickListener(this);
        checkUserLogInTable();

    }

    private boolean checkUserLogInTable() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase dbLocal = databaseHelper.getReadableDatabase();
        Cursor cursor = dbLocal.rawQuery("SELECT * FROM ClientLogInData", null);
        Log.d("MyLogs", String.valueOf(cursor.getCount()));
        if (cursor.moveToLast()) {
            progressDialog = ProgressDialog.show(this, "", "Login. Please wait...", true);
            String login = cursor.getString(cursor.getColumnIndex("login"));
            String password = cursor.getString(cursor.getColumnIndex("password"));
            cursor.close();
            dbLocal.close();
            workingWithServerClass.logIn(this, login, password);
            return true;
        } else {
            cursor.close();
            dbLocal.close();
            databaseHelper.clearChatTable();
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logInActivity_LoginButton:
                progressDialog = ProgressDialog.show(this, "", "Login. Please wait...", true);
                workingWithServerClass.logIn(this, logInActivity_LoginEditText.getText().toString(), logInActivity_PasswordEditText.getText().toString());
                break;
            case R.id.logInActivity_CreateUserButton:
                Intent intent = new Intent(this, CreateUserActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void logInIncorrectLoginData() {
        progressDialog.dismiss();
        Toast.makeText(this, "Login incorrect", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void logInCorrectLoginData(int loginedUserId) {
        progressDialog.dismiss();
        Toast.makeText(this, "Login correct", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainMenuActivity.class);
        Intent serviceIntent = new Intent(this, UpdateDataService.class);
        intent.putExtra("loginedAccountId", workingWithServerClass.getLoginedAccountId());
        serviceIntent.putExtra("loginedAccountId", workingWithServerClass.getLoginedAccountId());
        startService(serviceIntent);
        startActivity(intent);
        finish();
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

    @Override
    public void INSERTtoChatSuccessfully() {

    }

    @Override
    public void INSERTtoChatError() {

    }

}
