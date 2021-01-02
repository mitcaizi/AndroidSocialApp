package com.example.lab_7;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupLogin extends AppCompatActivity {
    private EditText email, password, displayname, phonenumber;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    Button signupBtn;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_login);
        email=findViewById(R.id.emailText);
        password=findViewById(R.id.passwordText);
        phonenumber=findViewById(R.id.phoneNumberText);
        displayname=findViewById(R.id.displayNameText);
        signupBtn=findViewById(R.id.signupBtn);
        mAuth=FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        updateUI();
    }

    private void updateUI() {
        if (currentUser!=null){
            findViewById(R.id.displayNameLayout).setVisibility(View.GONE);
            findViewById(R.id.phoneNumberLayout).setVisibility(View.GONE);
            signupBtn.setVisibility(View.GONE);
        }
    }

    private void saveUserDataToDB(){
        FirebaseDatabase database= FirebaseDatabase.getInstance();
        DatabaseReference userRef= database.getReference("Users");
        userRef.child(currentUser.getUid()).setValue(new User(displayname.getText().toString(),
                email.getText().toString(),phonenumber.getText().toString()));
    }

    public void Signup(View view){
        if(email.getText().toString().equals("")||password.getText().toString().equals("")
        ||phonenumber.getText().toString().equals("")||displayname.getText().toString().equals("")){
        Toast.makeText(this, "Please provide all information", Toast.LENGTH_SHORT).show();
        return;
        }
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        currentUser = authResult.getUser();
                        currentUser.sendEmailVerification().addOnSuccessListener(SignupLogin.this, new
                                OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SignupLogin.this, "Signup successful. Verification email Sent!",
                                                Toast.LENGTH_SHORT).show();
                                        saveUserDataToDB();
                                        updateUI();
                                    }
                                }).addOnFailureListener(SignupLogin.this, new OnFailureListener() {
                            @Override
                            public void onFailure( Exception e) {
                                Toast.makeText(SignupLogin.this, e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(this, new OnFailureListener() { @Override
        public void onFailure( Exception e) {
            Toast.makeText(SignupLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } });
    }


    public void ResetPassword(View view){
        if (email.getText().toString().equals("")){
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email.getText().toString()).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SignupLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(this, new OnSuccessListener<Void>() { @Override
        public void onSuccess(Void aVoid) {
            Toast.makeText(SignupLogin.this, "Email sent!", Toast.LENGTH_SHORT).show();
        } });
    }


    public void sendEmailVerification(View view){
        if (mAuth.getCurrentUser()==null){
            Toast.makeText(this, "Please login to resend verification email", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.sendEmailVerification()
                .addOnSuccessListener(SignupLogin.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SignupLogin.this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                        updateUI();
                    }
                }).addOnFailureListener(SignupLogin.this, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SignupLogin.this, e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Login(View view){
        if (email.getText().toString().equals("")||password.getText().toString().equals("")){
            Toast.makeText(this, "Please provide all information", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        currentUser=authResult.getUser();
                        if (currentUser.isEmailVerified()){
                            Toast.makeText(SignupLogin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupLogin.this, HomeActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(SignupLogin.this, "Please verify your email and password again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SignupLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
