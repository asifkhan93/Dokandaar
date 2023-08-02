package com.example.dokandaar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dokandaar.adapters.AdapterOrderShop;
import com.example.dokandaar.adapters.AdapterProductSeller;
import com.example.dokandaar.Constants;
import com.example.dokandaar.models.ModelOrderShop;
import com.example.dokandaar.models.ModelProduct;
import com.example.dokandaar.R;
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

public class MainSellerActivity extends AppCompatActivity {
    private TextView nameTv, storeNametv, emailtv, tabProductsTv, tabOrdersTv,filteredProductsTv,filteredOrdersTv;
    private EditText searchProductEt;
    private ImageButton logoutBtn,editProfileBtn,addProductBtn,filterProductBtn,filterOrderBtn;
    private ImageView profileIv;
    private RelativeLayout productsRl,ordersRl;
    private RecyclerView productsRv,ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);


        nameTv = findViewById(R.id.tvName);
        logoutBtn = findViewById(R.id.btLogout);
        editProfileBtn =findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        storeNametv =findViewById(R.id.tvStoreName);
        emailtv = findViewById(R.id.tvEmail);
        tabProductsTv = findViewById(R.id.tvTabProducts);
        tabOrdersTv = findViewById(R.id.tvTabOrders);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        ordersRv = findViewById(R.id.ordersRv);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filterOrderBtn = findViewById(R.id.filterOrdersBtn);
        profileIv = findViewById(R.id.ivProfile);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();

        showProductsUI();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterProductSeller.getFilter().filter(s);

                }catch (Exception e){
                    e.printStackTrace();

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


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
                //go to edit profile seller activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));

            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Edit product activity
                startActivity(new Intent(MainSellerActivity.this, EditProductActivity.class));



            }
        });
        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show products
                showProductsUI();

            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            //load orders
            @Override
            public void onClick(View v) {
                showOrdersUI();

            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }else {
                                    loadFilteredProducts(selected);
                                }

                            }
                        })
                        .show();
            }
        });
        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] options = {"All","In Progress","Completed", "Cancelled"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which==0){
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter("");
                                }
                                else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing "+optionClicked+"Orders");
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    private void loadAllOrders() {
        orderShopArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);

                            orderShopArrayList.add(modelOrderShop);
                        }

                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this,orderShopArrayList);

                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list

                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);

                            }


                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list

                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);

                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showProductsUI() {
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.black));
        tabProductsTv.setBackgroundResource(R.drawable.share_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTv.setBackgroundColor(getResources().getColor(R.color.white));
    }
    private void showOrdersUI() {
        ordersRl.setVisibility(View.VISIBLE);
        productsRl.setVisibility(View.GONE);
        tabProductsTv.setTextColor(getResources().getColor(R.color.white));
        tabProductsTv.setBackgroundColor(getResources().getColor(R.color.white));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTv.setBackgroundResource(R.drawable.share_rect04);


    }



    private void makeMeOffline() {        //make user online
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
                        Toast.makeText(MainSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));

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
                            //get data from db

                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("online").getValue();
                            String email = ""+ds.child("email").getValue();
                            String storeName = ""+ds.child("storeName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            nameTv.setText(name);
                            storeNametv.setText(storeName);
                            emailtv.setText(email);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_stroe_gray).into(profileIv);

                            }catch (Exception e){
                                profileIv.setImageResource(R.drawable.ic_stroe_gray);
                            }
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}