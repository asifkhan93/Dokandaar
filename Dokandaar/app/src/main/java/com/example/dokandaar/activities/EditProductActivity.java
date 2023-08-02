package com.example.dokandaar.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dokandaar.Constants;
import com.example.dokandaar.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt,descriptionEt;
    private TextView categoryTv, quantityTv, priceTv,discounterPriceTv,discountedNoteTv;
    private SwitchCompat discountSwitch;
    private Button addProductsBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE= 400;
    private static final int IMAGE_PICK_CAMERA_CODE= 400;
    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked uri
    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        backBtn = findViewById(R.id.btBack);
        titleEt = findViewById(R.id.etTitleText);
        descriptionEt= findViewById(R.id.etDescription);
        categoryTv =findViewById(R.id.tvCategory);
        quantityTv = findViewById(R.id.etQuantity);
        priceTv = findViewById(R.id.etPrice);
        discounterPriceTv = findViewById(R.id.etDiscountedPrice);
        discountedNoteTv = findViewById(R.id.etDiscountNote);
        discountSwitch = findViewById(R.id.discountSwitch);
        addProductsBtn = findViewById(R.id.addProductBt);
        productIconIv = findViewById(R.id.productIconIv);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        discounterPriceTv.setVisibility(View.GONE);
        discountedNoteTv.setVisibility(View.GONE);
        //init permission arrays
        cameraPermissions = new  String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    discounterPriceTv.setVisibility(View.VISIBLE);
                    discountedNoteTv.setVisibility(View.VISIBLE);
                }else{
                    discounterPriceTv.setVisibility(View.GONE);
                    discountedNoteTv.setVisibility(View.GONE);

                }
            }
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                showImagePickDialog();
            }
        });
        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });
        addProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                //validate data
                //add to db
                inputData();
            }
        });
    }
    private String productTitle,productDescription,productCategory,productQuantity,
         originalPrice,discountedPrice,discountNote;
    private boolean discountAvailable = false;
    private void inputData() {
        //input data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityTv.getText().toString().trim();
        originalPrice = priceTv.getText().toString().trim();
        discountAvailable =discountSwitch.isChecked();

        //validate data
        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Product Title required", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(productDescription)){
            Toast.makeText(this, "Product description required", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category required", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price required", Toast.LENGTH_SHORT).show();
        }
        if (discountAvailable){
            discountedPrice =discounterPriceTv.getText().toString().trim();
            discountNote = discountedNoteTv.getText().toString().trim();
            if (TextUtils.isEmpty(discountedPrice))
            Toast.makeText(this, "Discount price required", Toast.LENGTH_SHORT).show();

            return;

        }else {
            discountedPrice = "0";
            discountNote = "";

        }
        addProduct();

    }

    private void addProduct() {
        //add to db
        progressDialog.setMessage("Adding Product");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        if (image_uri == null){
            //upload without image
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("productId",""+timestamp);
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("productIcon","");
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountedPrice",""+discountedPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailable);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("uid",""+firebaseAuth.getUid());
            //add to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                            clearData();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        else {
            //upload with image
            //upload to storage
            //name and path of image to db
            String filPathAndName = "product_images/"+""+ timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filPathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded
                            //get url of image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            if (uriTask.isSuccessful()){
                                //url of image received
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("productId",""+timestamp);
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("productIcon",""+downloadImageUri);
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountedPrice",""+discountedPrice);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("discountAvailable",""+discountAvailable);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                //add to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                                                clearData();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
    }

    private void  clearData(){

        //clear data after uploading
        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityTv.setText("");
        priceTv.setText("");
        discounterPriceTv.setText("");
        productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_primarycolor);
        image_uri= null;
    }
    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get category
                        String category = Constants.productCategories[which];
                        //set category
                        categoryTv.setText(category);
                    }
                })
                .show();
    }


    private void showImagePickDialog() {
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //camera clicked
                            if (checkCameraPermission()){
                                //granted
                                pickFromCamera();
                            }else{
                                //denied
                                requestCameraPermission();
                            }

                        }else{
                            //gallery clicked
                           if (checkStoragePermission()){
                               pickFromGallery();


                           }else{
                               requestStoragePermission();

                           }

                        }

                    }
                }).show();
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }
    private void  requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);

    }
    private boolean checkCameraPermission() {

        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,CAMERA_REQUEST_CODE);


    }
    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted ){
                        pickFromCamera();

                    }else {
                        Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                    }
                    if (storageAccepted){
                        pickFromCamera();

                    }else {
                        Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();

                    }
                }

            }
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }else {
                        Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //handle image pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_PICK_GALLERY_CODE){
            //image from gallery
            image_uri = data.getData();
            //st image
            productIconIv.setImageURI(image_uri);
        }else if (requestCode == IMAGE_PICK_CAMERA_CODE){

            productIconIv.setImageURI(image_uri);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}