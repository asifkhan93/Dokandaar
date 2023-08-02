package com.example.dokandaar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dokandaar.R;
import com.example.dokandaar.models.ModelCartItem;
import com.example.dokandaar.models.ModelOrderedItem;

import java.util.ArrayList;

public class AdaptedOrderedItem extends RecyclerView.Adapter<AdaptedOrderedItem.HolderOrderedItem> {

    private Context context;
    private ArrayList<ModelOrderedItem> orderedItemArrayList;

    public AdaptedOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemArrayList) {
        this.context = context;
        this.orderedItemArrayList = orderedItemArrayList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_orderditem,parent,false);

        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {

        //get data at position
        ModelOrderedItem modelOrderedItem = orderedItemArrayList.get(position);
        String getpId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(name);
        holder.itemPriceEachTv.setText("Tk"+price);
        holder.itemPriceTv.setText("Tk"+cost);
        holder.itemQuantityTv.setText("[" + quantity + "]");

    }

    @Override
    public int getItemCount() {
        return orderedItemArrayList.size();
    }

    class HolderOrderedItem extends RecyclerView.ViewHolder{
          private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv;


          public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);


        }
    }
}
