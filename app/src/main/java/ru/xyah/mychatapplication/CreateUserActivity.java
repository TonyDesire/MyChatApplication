package ru.xyah.mychatapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.IOException;

public class CreateUserActivity extends AppCompatActivity implements View.OnClickListener, WorkingWithServerClass.Callback {

    EditText createUserActivity_LoginEditText;
    EditText createUserActivity_PasswordEditText;
    EditText createUserActivity_confirmPasswordEditText;
    EditText createUserActivity_firstNameEditText;
    EditText createUserActivity_secondNameEditText;
    EditText createUserActivity_middleNameEditText;
    EditText createUserActivity_ageEditText;
    Spinner createUserActivity_genderSpinner;
    EditText createUserActivity_phoneEditText;
    EditText createUserActivity_nationalityEditText;
    EditText createUserActivity_stateEditText;
    EditText createUserActivity_cityEditText;
    EditText createUserActivity_addressEditText;
    EditText createUserActivity_emailEditText;
    EditText createUserActivity_facultyEditText;
    Button createUserActivity_createAccountButton;
    ImageView createUserActivity_UserPhotoImageView;

    String[] genderSpinnerData = {"No", "Male", "Female"};
    ArrayAdapter<String> genderSpinnerAdapter;

    Uri selectedImage = null;

    AlertDialog.Builder allertDialog;
    ProgressDialog progressDialog;

    WorkingWithServerClass workingWithServerClass = new WorkingWithServerClass(this);

    static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        createUserActivity_LoginEditText = (EditText) findViewById(R.id.createUserActivity_LoginEditText);
        createUserActivity_PasswordEditText = (EditText) findViewById(R.id.createUserActivity_PasswordEditText);
        createUserActivity_confirmPasswordEditText = (EditText) findViewById(R.id.createUserActivity_confirmPasswordEditText);
        createUserActivity_firstNameEditText = (EditText) findViewById(R.id.createUserActivity_firstNameEditText);
        createUserActivity_secondNameEditText = (EditText) findViewById(R.id.createUserActivity_secondNameEditText);
        createUserActivity_middleNameEditText = (EditText) findViewById(R.id.createUserActivity_middleNameEditText);
        createUserActivity_ageEditText = (EditText) findViewById(R.id.createUserActivity_ageEditText);
        createUserActivity_genderSpinner = (Spinner) findViewById(R.id.createUserActivity_genderSpinner);
        createUserActivity_phoneEditText = (EditText) findViewById(R.id.createUserActivity_phoneEditText);
        createUserActivity_nationalityEditText = (EditText) findViewById(R.id.createUserActivity_nationalityEditText);
        createUserActivity_stateEditText = (EditText) findViewById(R.id.createUserActivity_stateEditText);
        createUserActivity_cityEditText = (EditText) findViewById(R.id.createUserActivity_cityEditText);
        createUserActivity_addressEditText = (EditText) findViewById(R.id.createUserActivity_addressEditText);
        createUserActivity_emailEditText = (EditText) findViewById(R.id.createUserActivity_emailEditText);
        createUserActivity_facultyEditText = (EditText) findViewById(R.id.createUserActivity_facultyEditText);
        createUserActivity_createAccountButton = (Button) findViewById(R.id.createUserActivity_createAccountButton);
        createUserActivity_UserPhotoImageView = (ImageView) findViewById(R.id.createUserActivity_UserPhotoImageView);

        workingWithServerClass.registerCallback(this);

        genderSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, genderSpinnerData);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        createUserActivity_genderSpinner.setAdapter(genderSpinnerAdapter);
        createUserActivity_genderSpinner.setPrompt("Gender");
        createUserActivity_genderSpinner.setSelection(0);

        createUserActivity_UserPhotoImageView.setOnClickListener(this);
        createUserActivity_createAccountButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createUserActivity_createAccountButton:
                if (!checkAllDataIsFilled()) {showErrorDialog("Error", "Fields Login, Password, First name, Second name and Age are required"); return;}
                if (!checkWhitespaces()) {showErrorDialog("Error", "Whitespaces in Login and Password are not allowed"); return;}
                if (!checkPassword()) {showErrorDialog("Error", "Password must consist of 4 or more characters"); return;}
                if (!checkPasswordsMatch()) {showErrorDialog("Error", "Incorrect Confirm Password"); return;}
                progressDialog = ProgressDialog.show(this, "", "Creating. Please wait...", true);
                workingWithServerClass.createUser(createUserActivity_LoginEditText.getText().toString(),
                        createUserActivity_PasswordEditText.getText().toString(),
                        createUserActivity_firstNameEditText.getText().toString().replace(" ", "%20"),
                        createUserActivity_secondNameEditText.getText().toString().replace(" ", "%20"),
                        createUserActivity_middleNameEditText.getText().toString().replace(" ", "%20"),
                        Integer.valueOf(createUserActivity_ageEditText.getText().toString()),
                        genderSpinnerData[createUserActivity_genderSpinner.getSelectedItemPosition()].replace(" ", "%20"),
                        createUserActivity_phoneEditText.getText().toString().replace(" ", "%20"),
                        createUserActivity_nationalityEditText.getText().toString().replace(" ", "%20"),
                        createUserActivity_stateEditText.getText().toString().replace(" ", "%20"),
                        createUserActivity_cityEditText.getText().toString().replace(" ", "%20"),
                        createUserActivity_addressEditText.getText().toString().replace(" ", "%20"),
                        createUserActivity_emailEditText.getText().toString().replace(" ", "%20"),
                        createUserActivity_facultyEditText.getText().toString().replace(" ", "%20"),
                        selectedImage);
                break;
            case R.id.createUserActivity_UserPhotoImageView:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            selectedImage = data.getData();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Log.d("MyLogs", selectedImage.getPath());
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImage.getPath(), options);
                createUserActivity_UserPhotoImageView.setImageBitmap(bitmap);

        }
    }

    private boolean checkAllDataIsFilled(){
        if (createUserActivity_LoginEditText.getText().toString().length() == 0) return false;
        if (createUserActivity_PasswordEditText.getText().toString().length() == 0) return false;
        if (createUserActivity_confirmPasswordEditText.getText().toString().length() == 0) return false;
        if (createUserActivity_firstNameEditText.getText().toString().length() == 0) return false;
        if (createUserActivity_secondNameEditText.getText().toString().length() == 0) return false;
        if (createUserActivity_ageEditText.getText().toString().length() == 0) return false;
      return true;
    }

    private boolean checkPassword(){
        if (createUserActivity_PasswordEditText.getText().toString().length() < 4) return false;
        return true;
    }

    private boolean checkPasswordsMatch(){
        if (!createUserActivity_PasswordEditText.getText().toString().equals(createUserActivity_confirmPasswordEditText.getText().toString())) return false;
        else return true;
    }

    private boolean checkWhitespaces(){
        if (createUserActivity_LoginEditText.getText().toString().indexOf(' ') != -1) return false;
        if (createUserActivity_PasswordEditText.getText().toString().indexOf(' ') != -1) return false;
        if (createUserActivity_confirmPasswordEditText.getText().toString().indexOf(' ') != -1) return false;
        return true;
    }

    private void showErrorDialog(String title,String text){
        allertDialog = new AlertDialog.Builder(this);
        allertDialog.setTitle(title);
        allertDialog.setMessage(text);
        allertDialog.setPositiveButton("Ok", null);
        allertDialog.show();
    }


    @Override
    public void createAccountSuccessfullyCreated() {
        progressDialog.dismiss();
        allertDialog = new AlertDialog.Builder(this);
        allertDialog.setCancelable(false);
        allertDialog.setTitle("Success");
        allertDialog.setMessage("Account successfully created");
        allertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        allertDialog.show();
    }

    @Override
    public void createAccountAlreadyExists() {
        progressDialog.dismiss();
        showErrorDialog("Error", "Account with this login already exists");
    }

    @Override
    public void createAccountNoConnDb() {
        progressDialog.dismiss();
        showErrorDialog("Error", "Error connection with server");
    }

    @Override
    public void INSERTtoChatSuccessfully() {

    }

    @Override
    public void INSERTtoChatError() {

    }

    @Override
    public void logInIncorrectLoginData() {

    }

    @Override
    public void logInCorrectLoginData(int loginedUserId) {

    }

}
