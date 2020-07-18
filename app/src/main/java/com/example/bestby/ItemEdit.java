package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemEdit extends AppCompatActivity {

    String userID;
    int productPosition;
    String documentID;
    TextView productName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);

        productName = findViewById(R.id.editProductName);

        userID = getIntent().getStringExtra("userID");
        productPosition = getIntent().getIntExtra("productPosition", -1);
        GetProductDetails();
    }

    public void GetProductDetails() {
        FirebaseFirestore.getInstance()
                .collection("users/" + userID + "/products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            myListOfDocuments = SortByDate(myListOfDocuments);
                            documentID = myListOfDocuments.get(productPosition).getId();
                            productName.setText(myListOfDocuments.get(productPosition).getString("product"));
                        }
                    }
                });
    }

    public void RemoveItemDialog(View view) {
        RemoveItemDialog rid = new RemoveItemDialog(ItemEdit.this, userID, productPosition);
        rid.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rid.show();
    }

    //TODO Add to a java class so it is not a duplicate method. also in main activity
    public List<DocumentSnapshot> SortByDate(List<DocumentSnapshot> myListOfDocuments) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        ArrayList<Date> dates = new ArrayList<>();
        for(int i = 0; i < myListOfDocuments.size(); i++) {
            try {
                dates.add(format.parse((String) myListOfDocuments.get(i).get("expiryDateDisplay")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int n = dates.size();
        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++)
                if (dates.get(j).after(dates.get(j+1)))
                {
                    Date tempDate = dates.get(j);
                    dates.set(j,dates.get(j+1));
                    dates.set(j+1, tempDate);

                    DocumentSnapshot tempDoc = myListOfDocuments.get(j);
                    myListOfDocuments.set(j,myListOfDocuments.get(j+1));
                    myListOfDocuments.set(j+1, tempDoc);
                }
        return myListOfDocuments;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}