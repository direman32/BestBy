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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.bestby.staticValues.REMOVED_PRODUCTS;

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
    private FirebaseFirestore fStoreRef;
    private TextView offlineList;


    public RemoveItemDialog() {
        super(new Activity());
    }

    public RemoveItemDialog(Activity a, String userID, int productPosition, FirebaseFirestore fStore, TextView view) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        fStoreRef = fStore;
        this.userID = userID;
        this.productPosition = productPosition;
        offlineList = view;
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
                Intent intent = new Intent(c.getApplicationContext(), MainActivity.class);
                //intent.putExtra(REMOVED_PRODUCTS, (Serializable) setUpRemovedProductList(fStoreRef));
                //ItemEdit.finishActivity(c);
                //c.startActivity(intent);

                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    private List<String> setUpRemovedProductList(FirebaseFirestore fStore) {
        //Make Serialized
        FirebaseFirestore.getInstance().collection("users/" + userID + "/removedProducts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        StringBuilder documentDetails = new StringBuilder();
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            for(int i = 0; i < myListOfDocuments.size(); i++) {
                                documentDetails.append(String.format("%s%s/>/>/>",
                                        myListOfDocuments.get(i).get(staticValues.PRODUCT_NAME_KEY).toString().trim(),
                                        myListOfDocuments.get(i).get(staticValues.PRODUCT_DATE_DISPLAY_KEY).toString().trim()));
                            }

                            offlineList.setText(documentDetails);
                        }
                    }
                });

        String[] tempArray = offlineList.getText().toString().split(">/>/>");

        return Arrays.asList(tempArray);
    }

    public void GetProductDetails() {
         fStoreRef
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
        final FirebaseFirestore db = fStoreRef;
        final boolean[] failed = {false};

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
                                                System.out.println("DocumentSnapshot added with ID: " + documentReference.getId());
                                                Toast.makeText(c, "Product Removed Successfully",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(c, "Could not remove product at this time",
                                                        Toast.LENGTH_SHORT).show();
                                                failed[0] = true;
                                            }
                                        })
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(!failed[0]) {
                                                db.collection("users/" + userID + "/products").document(documentID)
                                                        .delete();
                                            }
                                            Toast.makeText(c, "Added to removed Products",
                                                    Toast.LENGTH_SHORT).show();
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