package com.example.bestby;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SuggestedItemAdapter extends ArrayAdapter<suggestedProductView> {

    private Context context;
    private List<suggestedProductView> products;

    //constructor, call on creation
    public SuggestedItemAdapter(Context context, int resource, ArrayList<suggestedProductView> objects) {
        super(context, resource, objects);

        this.context = context;
        this.products = objects;
    }

    //called when rendering the list
    public View getView(int position, View convertView, ViewGroup parent) {

        suggestedProductView product = products.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.suggested_item, null);

        TextView name = (TextView) view.findViewById(R.id.productViewName);

        name.setText(product.getName());

        return view;
    }
}
