package com.shell.simplifiedsignin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView mTextViewEmail, mTextViewUId;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewEmail = (TextView) findViewById(R.id.text_view_email);
        mTextViewUId = (TextView) findViewById(R.id.text_view_uid);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = mAuth.getCurrentUser();
                if(user == null){
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }else{
                    mTextViewEmail.setText("Welcome!  "+user.getEmail());
                    mTextViewUId.setText("UId : " + user.getUid());
                }
            }
        };
    }

    private void deleteAccount(){
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Your profile is deleted:( Create a account now!\"",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                    finish();
                }else{
                    Toast.makeText(MainActivity.this,"Failed to delete your account",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //sign out method
    private void  signOut() {
        mAuth.signOut();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_item_change_email:
                return true;
            case R.id.menu_item_change_password:
                return true;
            case R.id.menu_item_reset_password:
                startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
                return true;
            case R.id.menu_item_delete_account:
                deleteAccount();
                return true;
            case R.id.menu_item_sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
