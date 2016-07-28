package com.iliayugai.deeds;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Created by highjump on 15-5-29.
 */
public class SigninActivity extends Activity implements View.OnClickListener {

    private EditText mEditUsername;
    private EditText mEditPassword;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signin);

        // init view
        Button button = (Button)findViewById(R.id.but_signin);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_signup);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_forget);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_back);
        button.setOnClickListener(this);

        mEditUsername = (EditText)findViewById(R.id.edit_username);
        mEditPassword = (EditText)findViewById(R.id.edit_password);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_signup:
                CommonUtils.moveNextActivity(this, SignupActivity.class, false);
                break;

            case R.id.but_signin:
                doSignin();
                break;

            case R.id.but_forget:
                CommonUtils.moveNextActivity(this, ForgetActivity.class, false);
                break;

            case R.id.but_back:
                onBackPressed();
                break;
        }
    }

    private void doSignin() {

        String strUsername = mEditUsername.getText().toString();
        String strPassword = mEditPassword.getText().toString();

        if (TextUtils.isEmpty(strUsername)) {
            CommonUtils.createErrorAlertDialog(this, "", "Input your email address").show();
            return;
        }

        if (TextUtils.isEmpty(strPassword)) {
            CommonUtils.createErrorAlertDialog(this, "", "Input your password").show();
            return;
        }

        mProgressDialog = ProgressDialog.show(this, "", "Signing in...");

        UserData.logInInBackground(strUsername, strPassword, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                mProgressDialog.dismiss();

                if (parseUser != null) {
                    CommonUtils.moveNextActivity(SigninActivity.this, MainActivity.class, true);
                }
                else {
                    CommonUtils.createErrorAlertDialog(SigninActivity.this, "", e.getLocalizedMessage()).show();
                }
            }
        });
    }
}
