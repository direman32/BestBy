package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RemovedProducts extends AppCompatActivity {

    FirebaseAuth fAuth;
    ListView removedProductsView;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_removed_products);

        fAuth = FirebaseAuth.getInstance();
        removedProductsView = findViewById(R.id.removedProductsList);
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        ShowProducts();
    }

    public void ShowProducts() {
        FirebaseFirestore.getInstance()
                .collection("users/" + userID + "/removedProducts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            FillListView(myListOfDocuments);
                        }
                    }
                });
    }

    public void FillListView(List<DocumentSnapshot> myListOfDocuments) {
        List<String> documentDetails = new ArrayList<>();

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your array as a third parameter.
        ArrayAdapter<String> arrayAdapter;
        arrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.list_item,
                documentDetails);


        myListOfDocuments = SortByDate(myListOfDocuments);
        for(int i = 0; i < myListOfDocuments.size(); i++) {
            documentDetails.add(String.format("%-17s %s", myListOfDocuments.get(i).get("product").toString().trim(),
                    myListOfDocuments.get(i).get("expiryDateDisplay").toString().trim()));
        }
        removedProductsView.setAdapter(arrayAdapter);

        removedProductsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

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
}