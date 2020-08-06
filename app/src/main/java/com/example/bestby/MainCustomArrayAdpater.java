package com.example.bestby;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainCustomArrayAdpater extends ArrayAdapter<productViewMain> {

    private Context context;
    private List<productViewMain> products;

    //constructor, call on creation
    public MainCustomArrayAdpater(Context context, int resource, ArrayList<productViewMain> objects) {
        super(context, resource, objects);

        this.context = context;
        this.products = objects;
    }

    //called when rendering the list
    public View getView(int position, View convertView, ViewGroup parent) {

        productViewMain product = products.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main_list_item, null);

        TextView name = (TextView) view.findViewById(R.id.productViewName);
        TextView date = (TextView) view.findViewById(R.id.productViewDisplayDate);
        TextView check = (TextView) view.findViewById(R.id.checkText);
        //ImageView image = (ImageView) view.findViewById(R.id.checkImage);

        name.setText(product.getName());
        date.setText(product.getDisplayDate());
        if(product.getCheck()) {
            check.setText("!");
           // int imageID = context.getResources().getIdentifier("exclamation", "drawable", context.getPackageName());
           // image.setImageResource(imageID);
        //    image.setImageDrawable(context.getResources().getDrawable(R.drawable.tick));
        }

        return view;
    }
}
