package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    EditText mEmail, mShop, mPassword, mConfirmPassword;
    Button registerButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmail              = findViewById(R.id.RegisterEmail);
        mShop               = findViewById(R.id.ShopName);
        mPassword           = findViewById(R.id.RegisterPassword);
        mConfirmPassword    = findViewById(R.id.ConfirmPassword);
        registerButton      = findViewById(R.id.RegisterButton);
        fAuth               = FirebaseAuth.getInstance();
        fStore              = FirebaseFirestore.getInstance();
        progressBar         = findViewById(R.id.progressBar);

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String shop = mShop.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String confirmPassword = mConfirmPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    mEmail.setError("Email Required");
                    return;
                }
                else if(TextUtils.isEmpty(shop)) {
                    mShop.setError("Shop Location Required");
                    return;
                }
                else if(TextUtils.isEmpty(password)) {
                    mPassword.setError("Password Required");
                    return;
                }
                else if(TextUtils.isEmpty(confirmPassword)) {
                    mConfirmPassword.setError("Confirm password Required");
                    return;
                }

                if(password.length() < 6) {
                    mPassword.setError("Password needs to be at least 6 characters long");
                }
                else if(!password.equals(confirmPassword)) {
                    mConfirmPassword.setError("Confirm password did not match password");
                }

                progressBar.setVisibility(View.VISIBLE);

                //register user

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("Email",email);
                            user.put("Shop",shop);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Register.this, "User Profile is created", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                }
                            });
                        }
                        else {
                            Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }
}