package com.iliayugai.deeds;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.parse.ParseException;
import com.parse.RequestPasswordResetCallback;

/**
 * Created by highjump on 15-5-29.
 */
public class ForgetActivity extends Activity implements View.OnClickListener {

    private EditText mEditEmail;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forget);

        // init view
        Button button = (Button)findViewById(R.id.but_signup);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_back);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_reset);
        button.setOnClickListener(this);

        mEditEmail = (EditText)findViewById(R.id.edit_email);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_back:
                onBackPressed();
                break;

            case R.id.but_signup:
                CommonUtils.moveNextActivity(this, SignupActivity.class, false);
                break;

            case R.id.but_reset:
                doReset();
                break;
        }
    }

    private void doReset() {

        String strEmail = mEditEmail.getText().toString();

        if (TextUtils.isEmpty(strEmail)) {
            CommonUtils.createErrorAlertDialog(this, "", "Input your email address").show();
            return;
        }

        mProgressDialog = ProgressDialog.show(this, "", "Sending Request...");

        UserData.requestPasswordResetInBackground(strEmail, new RequestPasswordResetCallback() {

            @Override
            public void done(ParseException e) {
                mProgressDialog.dismiss();

                if (e == null) {
                    CommonUtils.createErrorAlertDialog(ForgetActivity.this, "", "Request has been submitted").show();
                } else {
                    CommonUtils.createErrorAlertDialog(ForgetActivity.this, "", e.getLocalizedMessage()).show();
                }
            }
        });
    }
}
