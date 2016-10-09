package com.shell.simplifiedsignin;

import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout mTextInputLayoutEmail, mTextInputLayoutPassword;
    private EditText mEditTextEmail, mEditTextPassword;
    private Button mButtonSignUp, mButtonLogin, mButtonResetPassword;
    private ProgressBar mProgressbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }

        mTextInputLayoutEmail = (TextInputLayout) findViewById(R.id.text_input_layout_email);
        mTextInputLayoutPassword = (TextInputLayout) findViewById(R.id.text_input_layout_password);

        mEditTextEmail = (EditText) findViewById(R.id.edit_text_email);
        mEditTextPassword = (EditText) findViewById(R.id.edit_text_password);

        mButtonSignUp = (Button) findViewById(R.id.btn_sign_up);
        mButtonLogin = (Button) findViewById(R.id.btn_login);
        mButtonResetPassword = (Button) findViewById(R.id.btn_reset_password);

        mButtonSignUp.setOnClickListener(this);
        mButtonLogin.setOnClickListener(this);
        mButtonResetPassword.setOnClickListener(this);

        mProgressbar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_up:
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                return;
            case R.id.btn_login:
                signIn();
                return;
            case R.id.btn_reset_password:
                startActivity(new Intent(this, ResetPasswordActivity.class));
                return;
            default:
                Toast.makeText(LoginActivity.this, "No Click", Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn() {
        String email = mEditTextEmail.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();

        if (!validateForm(email, password)) {
            return;
        }

        mProgressbar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Unable to login", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mProgressbar.setVisibility(View.GONE);
    }

    //Validate Email and Password Field
    private boolean validateForm(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            mTextInputLayoutEmail.setError("Emaill Id Required");
            Toast.makeText(LoginActivity.this, "Email field required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isEmailValid(email)) {
            mTextInputLayoutEmail.setError("A valid Email Id is required");
            return false;
        } else {
            mTextInputLayoutEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            mTextInputLayoutPassword.setError("Password required");
            Toast.makeText(LoginActivity.this, "Password field required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isPasswordValid(password)) {
            mTextInputLayoutPassword.setError("Password must be at least 6 characters");
            return false;
        } else {
            mTextInputLayoutPassword.setError(null);
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

    //Check password length
    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressbar.setVisibility(View.GONE);
    }
}
