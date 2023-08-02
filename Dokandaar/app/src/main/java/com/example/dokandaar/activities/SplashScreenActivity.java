package com.example.dokandaar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.example.dokandaar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        firebaseAuth = FirebaseAuth.getInstance();

        //main activity after 2 sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user==null){
                    //user not logged in
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                    finish();

                }else{
                    // logged in
                    checkUserType();

                }

            }
        }, 1000);
    }
    private void checkUserType() {
        //check user type and start user/seller activity

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String accountType = ""+dataSnapshot.child("accountType").getValue();
                        if (accountType.equals("Seller")){

                            //user = seller
                            startActivity(new Intent(SplashScreenActivity.this, MainSellerActivity.class));
                            finish();

                        }else{

                            //user = user
                            startActivity(new Intent(SplashScreenActivity.this, MainUserActivity.class));
                            finish();


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}