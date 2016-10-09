package com.shell.signintest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private TextInputLayout mTextInputLayoutEmail;
    private TextInputLayout mTextInputLayoutPassword;

    private EditText mEditTextViewEmail;
    private EditText mEditTextViewPassword;

    private TextView mTextViewLoggedEmail;
    private TextView mTextViewLoggedUId;

    private Button mButtonSignIn;
    private Button mButtonSignUp;

    private SignInButton signInButton;

    //Firebase Variables
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Google variables
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextInputLayoutEmail = (TextInputLayout) findViewById(R.id.text_input_layout_email);
        mTextInputLayoutPassword = (TextInputLayout) findViewById(R.id.text_input_layout_password);

        mEditTextViewEmail = (EditText) findViewById(R.id.email_text);
        mEditTextViewPassword = (EditText) findViewById(R.id.password_text);

        mTextViewLoggedEmail = (TextView) findViewById(R.id.logged_email_id);
        mTextViewLoggedUId = (TextView) findViewById(R.id.logged_uid);

        mButtonSignIn = (Button) findViewById(R.id.email_sign_in_button);
        mButtonSignUp = (Button) findViewById(R.id.email_create_account_button);

        signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        setGoogleSignInButtonText(signInButton, "Sign in with Google");

        mButtonSignIn.setOnClickListener(this);
        mButtonSignUp.setOnClickListener(this);
        signInButton.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Intialize Firebasse Variables
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in
                    Toast.makeText(MainActivity.this, "Loged In", Toast.LENGTH_SHORT).show();

                    signInButton.setEnabled(false);
                    mButtonSignIn.setEnabled(false);
                    mButtonSignUp.setEnabled(false);
                    //startActivity(new Intent(MainActivity.this, homeActivity.class));
                } else {
                    //User is signed out
                    signInButton.setEnabled(true);
                    mButtonSignIn.setEnabled(true);
                    mButtonSignUp.setEnabled(true);
                }

                updateUI(user);
            }
        };
    }

    //Create new Account(Sign Up)
    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Unable to creat Account", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //sign In with Email and Password
    private void signInWithEmailAndPassword(String email, String password) {
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Unable to login", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Sign In with Google
    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google Sign In failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //Sign out
    private void signOut() {

        //Firebase Sign Out
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);

        updateUI(null);
        Toast.makeText(MainActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();

    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mTextViewLoggedEmail.setText(user.getEmail().toString());
            mTextViewLoggedUId.setText(user.getUid());
        } else {
            mTextViewLoggedEmail.setText(null);
            mTextViewLoggedUId.setText(null);
        }
        mEditTextViewEmail.setText(null);
        mEditTextViewPassword.setText(null);
    }

    //Validate Email and Password Field
    private boolean validateForm() {

        String email = mEditTextViewEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mTextInputLayoutEmail.setError("Emaill Id Required");
            Toast.makeText(MainActivity.this, "Email field required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isEmailValid(email)) {
            mTextInputLayoutEmail.setError("A valid Email id is required");
            return false;
        } else {
            mTextInputLayoutEmail.setError(null);
        }

        String password = mEditTextViewPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mTextInputLayoutPassword.setError("Password required");
            Toast.makeText(MainActivity.this, "Password field required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isPasswordValid(password)) {
            mTextInputLayoutPassword.setError("Password must be at least 6 characters");
            return false;
        } else {
            mTextInputLayoutPassword.setError(null);
        }

        return true;
    }

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

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.email_create_account_button) {
            createAccount(mEditTextViewEmail.getText().toString(), mEditTextViewPassword.getText().toString());
        } else if (id == R.id.email_sign_in_button) {
            signInWithEmailAndPassword(mEditTextViewEmail.getText().toString(), mEditTextViewPassword.getText().toString());
        } else if (id == R.id.google_sign_in_button) {
            signInWithGoogle();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater infalter = new MenuInflater(this);
        infalter.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_sign_out:
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    signOut();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
