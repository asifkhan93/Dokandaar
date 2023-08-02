package com.example.dokandaar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.dokandaar.R;
import com.example.dokandaar.adapters.AdaptedOrderedItem;
import com.example.dokandaar.adapters.AdapterCartItem;
import com.example.dokandaar.models.ModelOrderedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUsersActivity extends AppCompatActivity {
    private  String orderTo, orderId;


    private ImageButton backBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, shopNameTv, totalItemsTv, amountTv, addressTv;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdaptedOrderedItem adaptedOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_users);

        backBtn = findViewById(R.id.btBack);
        orderIdTv = findViewById(R.id.orderIdIv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);



        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");
        orderId = intent.getStringExtra("orderId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadOrderedItems() {

        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        orderedItemArrayList.clear();
                        for (DataSnapshot ds: datasnapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);

                            orderedItemArrayList.add(modelOrderedItem);

                        }
                        adaptedOrderedItem = new AdaptedOrderedItem(OrderDetailsUsersActivity.this, orderedItemArrayList);

                        itemsRv.setAdapter(adaptedOrderedItem);
                        totalItemsTv.setText(""+datasnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        orderedItemArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                        //get data
                        String orderBy = ""+datasnapshot.child("orderBy").getValue();
                        String orderCost = ""+datasnapshot.child("orderCost").getValue();
                        String orderId = ""+datasnapshot.child("orderId").getValue();
                        String orderStatus = ""+datasnapshot.child("orderStatus").getValue();
                        String orderTime = ""+datasnapshot.child("orderTime").getValue();
                        String orderTo = ""+datasnapshot.child("orderTo").getValue();
                        String deliveryFee = ""+datasnapshot.child("deliveryFee").getValue();
                        String latitude = ""+datasnapshot.child("latitude").getValue();
                        String longitude = ""+datasnapshot.child("longitude").getValue();

                        //convert timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();

                        if (orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.red));

                            //set data
                            orderIdTv.setText(orderId);
                            orderStatusTv.setText(orderStatus);
                            amountTv.setText("Tk"+orderCost+deliveryFee);
                            dateTv.setText(formatedDate);

                            findAddress(latitude, longitude);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadShopInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        String shopName = ""+datasnapshot.child("storeName").getValue();
                        shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon,1);

            String address = addresses.get(0).getAddressLine(0);
            addressTv.setText(address);

        }catch (Exception e){}

    }
}