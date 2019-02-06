package com.learn.firebasesocialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout lyoSignUp_Root;
    private EditText edtSignUp_Email, edtSignUp_Username, edtSignUp_Password;
    private Button btnSignUp_Register, btnSignUp_SignIn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        assignUI();
        initializeVars();

        FirebaseApp.initializeApp(this);

        // Call All OnClick Event Handler
        callAllOnClickEvent();
    }

    private void assignUI() {
        lyoSignUp_Root = findViewById(R.id.lyoSignUp_Root);
        edtSignUp_Email = findViewById(R.id.edtSignUp_Email);
        edtSignUp_Username = findViewById(R.id.edtSignUp_Username);
        edtSignUp_Password = findViewById(R.id.edtSignUp_Password);
        btnSignUp_Register = findViewById(R.id.btnSignUp_Register);
        btnSignUp_SignIn = findViewById(R.id.btnSignUp_SignIn);
    }

    private void initializeVars() {
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Transition to next activity
            transitiontoSocialMeiaActivity();
        }
    }

    private void transitiontoSocialMeiaActivity() {
        Intent intent = new Intent(this, SocialMediaActivity.class);
        startActivity(intent);
    }

    private void callAllOnClickEvent() {
        btnSignUp_Register.setOnClickListener(this);
        btnSignUp_SignIn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.btnSignUp_Register) :
                onClick_SignUpUser();
                break;
            case (R.id.btnSignUp_SignIn) :
                onClick_signInUser();
                break;
        }
    }

    private void onClick_SignUpUser() {
        boolean isComplete = false;

        // Check all fields
        if (edtSignUp_Email.getText().toString().equals("") ||
                edtSignUp_Username.getText().toString().equals("") ||
                edtSignUp_Password.getText().toString().equals("")) {
            Toast.makeText(this,
                    "Please fill all the required fields",
                    Toast.LENGTH_SHORT)
                .show();
        } else {
            isComplete = true;
        }

        if (isComplete) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Please wait...");
            dialog.show();

            mAuth.createUserWithEmailAndPassword(edtSignUp_Email.getText().toString(), edtSignUp_Password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this,
                                        "Signing up SUCCESSFUL",
                                        Toast.LENGTH_SHORT)
                                        .show();

                                FirebaseDatabase.getInstance().getReference().child("my_users")
                                        .child(task.getResult().getUser().getUid()).child("username")
                                        .setValue(edtSignUp_Username.getText().toString());


                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(edtSignUp_Username.getText().toString())
                                        .build();

                                FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SignUpActivity.this, "Display name updated", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                transitiontoSocialMeiaActivity();
                            } else {
                                Toast.makeText(SignUpActivity.this,
                                        "Signing up failed",
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
        }
    }

    private void onClick_signInUser() {
        boolean isComplete = false;

        // Check all fields
        if (edtSignUp_Email.getText().toString().equals("") ||
                edtSignUp_Password.getText().toString().equals("")) {
            Toast.makeText(this,
                    "Please fill all the required fields",
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            isComplete = true;
        }

        if (isComplete) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Please wait...");
            dialog.show();

            mAuth.signInWithEmailAndPassword(edtSignUp_Email.getText().toString(), edtSignUp_Password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this,
                                        "Signing in SUCCESSFUL",
                                        Toast.LENGTH_SHORT)
                                        .show();
                                transitiontoSocialMeiaActivity();
                            } else {
                                Toast.makeText(SignUpActivity.this,
                                        "Signing in failed",
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
        }

    }

}