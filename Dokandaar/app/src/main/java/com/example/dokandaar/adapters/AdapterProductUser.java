package com.example.dokandaar.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dokandaar.FilterProductUser;
import com.example.dokandaar.R;
import com.example.dokandaar.activities.ShopDetailActivity;
import com.example.dokandaar.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;


public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser>implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productsList = productList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent,false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {

        //get data
        final ModelProduct modelProduct = productsList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountedPrice();
        String productCategory = modelProduct.getProductCategory();
        String originalPrice = modelProduct.getOriginalPrice();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon =modelProduct.getProductIcon();

        //set data
        holder.titleTv.setText(productTitle);
        holder.discountedNoteTv.setText(discountNote);
        holder.descriptionTv.setText(productDescription);
        holder.originalPriceTv.setText("Tk"+originalPrice);
        holder.discountedPriceTv.setText("Tk"+discountPrice);
        if (discountAvailable.equals("true")){
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);

        }else{
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_baseline_add_shopping_primarycolor).into(holder.productIconIv);

        }catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_primarycolor);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product to cart
                showQuantityDialog(modelProduct);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product detail

            }
        });



    }
    private double cost = 0;
    private  double finalCost = 0;
    private int quantity = 0;
    private void showQuantityDialog(ModelProduct modelProduct) {
        //inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        //init layout views
        ImageView productIv = view.findViewById(R.id.productIv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView descriptionTv= view.findViewById(R.id.descriptionTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        TextView finalPriceTv =view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //get data from model
        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();

       final String  price;
        if (modelProduct.getDiscountAvailable().equals("true")){
            //product have discount
            price = modelProduct.getDiscountedPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);


        }else {
            discountedNoteTv.setVisibility(View.GONE);
            priceDiscountedTv.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();

        }
        cost = Double.parseDouble(price.replaceAll("Tk", ""));
        finalCost = Double.parseDouble(price.replaceAll("Tk",""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        //set data
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_shopping_cart_gray).into(productIv);

        }
        catch (Exception e){
            productIv.setImageResource(R.drawable.ic_shopping_cart_gray);
        }
        titleTv.setText(""+title);
        pQuantityTv.setText(""+productQuantity);
        descriptionTv.setText(""+description);
        discountedNoteTv.setText(""+discountNote);
        quantityTv.setText(""+quantity);
        originalPriceTv.setText("Tk"+modelProduct.getOriginalPrice());
        priceDiscountedTv.setText("Tk"+modelProduct.getDiscountedPrice());
        finalPriceTv.setText("Tk"+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity++;

                finalPriceTv.setText("Tk"+finalCost);
                quantityTv.setText(""+quantity);
            }
        });
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity>1){
                    finalCost = finalCost - cost;
                    quantity--;
                    finalPriceTv.setText("TK"+finalCost);
                    quantityTv.setText(""+quantity);
                }
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalPriceTv.getText().toString().trim().replace("Tk","");
                String quantity = quantityTv.getText().toString().trim();

                //add to db
                addToCart(productId, title, priceEach, totalPrice, quantity);

                dialog.dismiss();


            }
        });
    }
    private int itemId = 1;
    private void addToCart(String productId, String title, String priceEach, String price,String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
               // .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                //.addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();
        Toast.makeText(context, "Added to cart..", Toast.LENGTH_SHORT).show();


        ((ShopDetailActivity)context).cartCount();

    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {

        if (filter == null){
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }


    class  HolderProductUser extends RecyclerView.ViewHolder{


        //ui views
        private ImageView productIconIv;
        private TextView discountedNoteTv, titleTv, descriptionTv, addToCartTv,
        discountedPriceTv,originalPriceTv;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            //init ui views
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);





        }
    }
}
