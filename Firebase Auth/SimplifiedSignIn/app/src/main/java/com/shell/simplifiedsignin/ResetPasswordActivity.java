package com.shell.simplifiedsignin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout mTextInputLayoutEmail;
    private EditText mEditTextEmail;
    private Button mButtonResetPassword, mButtonBack;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        mTextInputLayoutEmail = (TextInputLayout) findViewById(R.id.text_input_layout_email);

        mEditTextEmail = (EditText) findViewById(R.id.edit_text_email);

        mButtonResetPassword = (Button) findViewById(R.id.btn_reset_password);
        mButtonBack = (Button) findViewById(R.id.btn_back);

        mButtonResetPassword.setOnClickListener(this);
        mButtonBack.setOnClickListener(this);

        mProgressbar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset_password:
                resetPassword();
                return;
            case R.id.btn_back:
                finish();
        }
    }

    private void resetPassword() {
        String email = mEditTextEmail.getText().toString().trim();

        if (!validateEmail(email)) {
            return;
        }

        mProgressbar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Validate Email
    private boolean validateEmail(String email) {

        if (TextUtils.isEmpty(email)) {
            mTextInputLayoutEmail.setError("Emaill Id Required");
            Toast.makeText(ResetPasswordActivity.this, "Email field required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isEmailValid(email)) {
            mTextInputLayoutEmail.setError("A valid Email Id is required");
            return false;
        } else {
            mTextInputLayoutEmail.setError(null);
        }
        return true;
    }

    //Check Email
    private boolean isEmailValid(String email) {

        boolean isValid = false;

        CharSequence emailString = email;

        Pattern pattern = Patterns.EMAIL_ADDRESS;
        Matcher matcher = pattern.matcher(emailString);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressbar.setVisibility(View.GONE);
        mEditTextEmail.setText(null);
    }
}
