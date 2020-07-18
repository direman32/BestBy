package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
import java.util.List;
import java.util.Map;

public class RemoveItemDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;
    public TextView message;
    public TextView numberRemoved;
    private String userID;
    private int productPosition;
    private String documentID;


    public RemoveItemDialog() {
        super(new Activity());
    }

    public RemoveItemDialog(Activity a, String userID, int productPosition) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;

        this.userID = userID;
        this.productPosition = productPosition;
        GetProductDetails();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_remove_item_dialog);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        message = findViewById(R.id.txt_dia);
        numberRemoved = findViewById(R.id.numberRemoved);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                if(TextUtils.isEmpty(numberRemoved.getText())) {
                    numberRemoved.setError("Quantity Required");
                    return;
                }
                RemoveProduct();
                c.finish();
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
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
                            message.setText("How many " + myListOfDocuments.get(productPosition).getString(staticValues.PRODUCT_NAME_KEY) + "(s) do you want to remove?");
                        }
                    }
                });
    }

    public void RemoveProduct() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users/" + userID + "/products").document(documentID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> map = document.getData();
                                map.put("numberRemoved", numberRemoved.getText().toString());
                                db.collection("users/" + userID +"/removedProducts")
                                        .add(map)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                db.collection("users/" + userID + "/products").document(documentID)
                                                        .delete();
                                                System.out.println("DocumentSnapshot added with ID: " + documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                System.out.println("Error adding document: "+ e.getMessage());
                                            }
                                        });
                            }
                        } else {
                            Log.d("Document Ref", "get failed with ", task.getException());
                        }
                    }
                });
    }

    //TODO Add to a java class so it is not a duplicate method. also in main activity
    public List<DocumentSnapshot> SortByDate(List<DocumentSnapshot> myListOfDocuments) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        ArrayList<Date> dates = new ArrayList<>();
        for(int i = 0; i < myListOfDocuments.size(); i++) {
            try {
                dates.add(format.parse((String) myListOfDocuments.get(i).get(staticValues.PRODUCT_DATE_DISPLAY_KEY)));
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