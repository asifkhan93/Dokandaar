package com.example.dokandaar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.dokandaar.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton backBt;
    private EditText emailEt;
    private Button recoverBt;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEt = findViewById(R.id.etEmail);
        backBt = findViewById(R.id.btBack);
        recoverBt =findViewById(R.id.btRecover);

       firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);



        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recoverBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recoverPassword();

            }
        });

    }

    private String email;
    private void recoverPassword() {
        email = emailEt.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid Email",Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Sending instructions to reset password");
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //instruction sent
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this,"Password reset instructions sent to your email",Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }
}