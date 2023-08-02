package com.example.dokandaar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dokandaar.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt;
    private TextView forgotPassTv , noAccountTv;
    private Button loginBt;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.etEmail);
        passwordEt = findViewById(R.id.etPassword);
        forgotPassTv =findViewById(R.id.tvForgotPassword);
        noAccountTv = findViewById(R.id.tvNoAccount);
        loginBt = findViewById(R.id.btLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
            }
        });

        forgotPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

    }
    private String email, password;

    private void loginUser() {
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid email pattern..",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Enter Password",Toast.LENGTH_SHORT).show();

            return;
        }

        progressDialog.setMessage("Logging in");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Successful Login
                        makeMeOnline();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed login
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();


                    }
                });

    }

    private void makeMeOnline() {
        //make user online
        progressDialog.setMessage("Checking User..");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","true");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //success
                        checkUserType();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkUserType() {
        //check user type and start user/seller activity

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            if (accountType.equals("Seller")){
                                progressDialog.dismiss();
                                //user = seller
                                startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                                finish();

                            }else{
                                progressDialog.dismiss();
                                //user = user
                                startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                                finish();


                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}