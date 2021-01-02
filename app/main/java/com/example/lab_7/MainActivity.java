package com.example.lab_7;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();


    }

    @Override
    protected void onResume() {
        super.onResume();
        new CountDownTimer(3000,1000){
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (currentUser==null){
                    Toast.makeText(MainActivity.this, "No user found", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, SignupLogin.class));
                    finish();
                }else {
                    if (currentUser.isEmailVerified()){
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Please verify your email and login.",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, SignupLogin.class));
                        finish();
                    }
                }
            }
        }.start();
    }
}
