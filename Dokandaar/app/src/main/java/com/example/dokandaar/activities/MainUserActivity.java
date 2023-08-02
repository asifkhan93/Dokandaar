package com.example.dokandaar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dokandaar.R;
import com.example.dokandaar.adapters.AdapterOrderUser;
import com.example.dokandaar.adapters.AdapterShop;
import com.example.dokandaar.models.ModelOrderUser;
import com.example.dokandaar.models.ModelShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTv,emailTv, phoneTv, tabShopsTv,tabOrdersTv;
    private RelativeLayout shopRl,ordersRl;
    private ImageView logoutBtn, editProfileBtn;
    private ImageView profileIv;
    private RecyclerView shopRv,ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);


        nameTv = findViewById(R.id.tvName);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopsTv= findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        logoutBtn = findViewById(R.id.btLogout);
        editProfileBtn =findViewById(R.id.btEditProfile);
        profileIv =findViewById(R.id.profileIv);

        shopRl = findViewById(R.id.shopRl);
        ordersRl = findViewById(R.id.ordersRl);
        shopRv =findViewById(R.id.shopRv);
        ordersRv = findViewById(R.id.ordersRv);

        firebaseAuth =FirebaseAuth.getInstance();
        checkUser();

        showShopUI();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to edit profile user activity
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));

            }
        });
        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShopUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrdersUI();
            }
        });
    }

    private void showShopUI() {
        shopRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.black));
        tabShopsTv.setBackgroundResource(R.drawable.share_rect04);


        tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTv.setBackgroundColor(getResources().getColor(R.color.white));
    }
    private void showOrdersUI() {
        shopRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.white));
        tabShopsTv.setBackgroundColor(getResources().getColor(R.color.white));


        tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTv.setBackgroundResource(R.drawable.share_rect04);
    }

    private void makeMeOffline() {
        //make user online
        progressDialog.setMessage("Logging Out");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //success
                        firebaseAuth.signOut();
                        checkUser();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));

        }else{
            loadMyInfo();

        }
    }

    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //get user data

                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("mobileNumber").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("online").getValue();
                            String city = ""+ds.child("city").getValue();

                            //set user data
                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(profileIv);
                            }catch (Exception e){
                                profileIv.setImageResource(R.drawable.ic_person_gray);

                            }
                            loadShops(city);
                            loadOrders();

                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        ordersList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (dataSnapshot.exists()){
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, ordersList);

                                        ordersRv.setAdapter(adapterOrderUser);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(String myCity) {
        //init list
        shopsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = ""+ds.child("city").getValue();
                            //show only user's city shops
                            if (shopCity.equals(myCity)){
                                shopsList.add(modelShop);
                            }
                            //to display all shops
                           // shopsList.add(modelShop);

                        }
                        //setup adapter
                        adapterShop = new AdapterShop(MainUserActivity.this,shopsList);

                        //set adapter
                        shopRv.setAdapter(adapterShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}