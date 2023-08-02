package com.example.dokandaar;
import android.widget.Filter;

import com.example.dokandaar.adapters.AdapterProductSeller;
import com.example.dokandaar.adapters.AdapterProductUser;
import com.example.dokandaar.models.ModelProduct;

import java.util.ArrayList;

public class FilterProductUser extends Filter {

    private AdapterProductUser adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProductUser(AdapterProductUser adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // validate data for searching
        if (constraint != null && constraint.length()>0){
            //search filed not empty ,searching something

            constraint = constraint.toString().toUpperCase();
            //store filtered list
            ArrayList<ModelProduct> filteredModels = new ArrayList<>();
            for (int i=0;i<filterList.size();i++){
                //check,search by title and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) || filterList.get(i).getProductCategory().toUpperCase().contains(constraint) ){
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));

                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }else{
            //search filed  empty , return original list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.productsList=(ArrayList<ModelProduct>)results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();

    }
}
