package com.example.dokandaar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dokandaar.R;
import com.example.dokandaar.activities.ShopDetailActivity;
import com.example.dokandaar.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       //inflate layout row_cartitem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {

        //get data
        ModelCartItem modelCartItem = cartItems.get(position);
        String id = modelCartItem.getId();
        String getpId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantity.setText("["+quantity+"]");
        holder.itemPriceEachTv.setText(""+price);

        //handle remove click listener, delete item
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will create table if not exists. but in that case will must exit
                EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Remove from cart..", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx = Double.parseDouble((((ShopDetailActivity)context).allTotalPriceTv.getText().toString().trim().replace("Tk","")));
                double totalPrice = tx - Double.parseDouble(cost.replace("Tk",""));
                double deliveryFee = Double.parseDouble((((ShopDetailActivity)context).deliveryFee.replace("Tk","")));
                double sTotalPrice = Double.parseDouble(String.format("%.2f",totalPrice)) - Double.parseDouble(String.format("%.2f", deliveryFee));
                ((ShopDetailActivity)context).allTotalPrice=0.00;
                ((ShopDetailActivity)context).sTotalTv.setText("Tk"+String.format("%.2f", sTotalPrice));
                ((ShopDetailActivity)context).allTotalPriceTv.setText("Tk"+String.format("%.2f", Double.parseDouble(String.format("%.2f", totalPrice))));

                ((ShopDetailActivity)context).cartCount();

            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size(); //return number of record
    }

    //view holder class
    class HolderCartItem extends RecyclerView.ViewHolder{
        //ui views of row_cartitem.cml
        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantity,itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            //init views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);




        }
    }
}
