package com.example.dokandaar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dokandaar.Constants;
import com.example.dokandaar.R;
import com.example.dokandaar.adapters.AdapterCartItem;
import com.example.dokandaar.adapters.AdapterProductUser;
import com.example.dokandaar.models.ModelCartItem;
import com.example.dokandaar.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailActivity extends AppCompatActivity {
    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv,filteredOrdersTv,
    addressTv, filteredProductsTv,cartCountTv;
    private ImageButton callBtn,cartBtn, mapBtn, backBtn, filterProductBtn,filterOrdersBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv,ordersRv;

    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    public String deliveryFee;
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartItem>  cartItemList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn =findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        ordersRv = findViewById(R.id.ordersRv);
        cartCountTv = findViewById(R.id.cartCountTv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrdersBtn = findViewById(R.id.filterOrdersBtn);

        //get uid from shop intent
        shopUid = getIntent().getStringExtra("shopUid");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();

        //if user add items to cart and go back and opens another cart in another shop
        //then cart should be different
        deleteCartData();
        cartCount();
        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterProductUser.getFilter().filter(s);

                }catch (Exception e){
                    e.printStackTrace();

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCartDialog();
            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openMap();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")){
                                    //load all
                                    loadShopProducts();
                                }else {
                                    adapterProductUser.getFilter().filter(selected);
                                }

                            }
                        })
                        .show();

            }
        });
    }

    private void deleteCartData() {


        easyDB.deleteAllDataFromTable(); //delete records from cart

    }
    public void cartCount(){
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count <=0){
            cartCountTv.setVisibility(View.GONE);
        }else{
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);
        }

    }

    public double allTotalPrice = 0.00;
    public TextView sTotalTv,dFeeTv,allTotalPriceTv;
    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();
        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init view
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
         sTotalTv = view.findViewById(R.id.sTotalTv);
         dFeeTv = view.findViewById(R.id.dFeeTv);
         allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn  = view.findViewById(R.id.checkoutBtn);
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();

        //get all the records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(0);
            String pId = res.getString(1);
            String name = res.getString(2);
            String price = res.getString(3);
            String cost = res.getString(4);
            String quantity = res.getString(5);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity);

            cartItemList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set to recycleview
        cartItemsRv.setAdapter(adapterCartItem);

        dFeeTv.setText("Tk"+deliveryFee);
        sTotalTv.setText("Tk"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("Tk"+(allTotalPrice + Double.parseDouble(deliveryFee.replace("Tk",""))));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLatitude.equals("")|| myLatitude.equals("null") || myLongitude.equals("")|| myLongitude.equals("null")){
                    Toast.makeText(ShopDetailActivity.this, "Please enter your address in your profile...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (myPhone.equals("")|| myPhone.equals("null")){
                    Toast.makeText(ShopDetailActivity.this, "Please enter your phone in your profile...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cartItemList.size() == 0){
                    Toast.makeText(ShopDetailActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();
            }
        });

    }

    private void submitOrder() {
        progressDialog.setMessage("Placing Order...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTv.getText().toString().trim().replace("Tk","");

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", ""+cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);


        final  DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        for (int i=0; i<cartItemList.size();i++){
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String,String> hashMap1 = new HashMap<>();
                            hashMap1.put("pID",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();

                        //open order details
                        Intent intent = new Intent(ShopDetailActivity.this, OrderDetailsUsersActivity.class);
                        intent.putExtra("orderTo", shopUid);
                        intent.putExtra("orderId",timestamp);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShopDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();



                    }
                });


    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);

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
                            myPhone = ""+ds.child("mobileNumber").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("online").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude =""+ds.child("longitude").getValue();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String name = ""+dataSnapshot.child("name").getValue();
                    shopName = ""+dataSnapshot.child("storeName").getValue();
                    shopEmail = ""+dataSnapshot.child("email").getValue();
                    shopPhone = ""+dataSnapshot.child("mobileNumber").getValue();
                    shopLatitude = ""+dataSnapshot.child("latitude").getValue();
                    shopLongitude = ""+dataSnapshot.child("longitude").getValue();
                    shopAddress = ""+dataSnapshot.child("address").getValue();
                    deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                String shopOpen = ""+dataSnapshot.child("shopOpen").getValue();


                    //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery fee : Tk"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("True")){
                    openCloseTv.setText("Open");

                }else{
                    openCloseTv.setText("Closed");

                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);

                }catch (Exception e){

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.parse(shopPhone))));
       Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadShopProducts() {
        productsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        productsList.clear();

                        for (DataSnapshot ds:dataSnapshot.getChildren()){

                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailActivity.this,productsList);
                        //set adapter
                        productsRv.setAdapter(adapterProductUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}