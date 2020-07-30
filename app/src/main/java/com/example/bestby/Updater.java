package com.example.bestby;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Updater implements Serializable {

    private Map<String, Object> documentUpdates;
    private static ArrayList<Map<String, Object>> AddUpdates;
    private static ArrayList<Map<String, Object>> EditUpdates;
    private static String userID;
    //documentType contains, type, documentID(if update), productName, expiryDate etc...

    public Updater(String userIDTemp) {
        documentUpdates = new HashMap<>();
        userID = userIDTemp;
    }

    public void addNewProduct(Map<String, Object> userDetails) {
        AddUpdates.add(userDetails);
    }

    public boolean UpdateNewProducts() {
        final boolean[] success = new boolean[1];
        for(int i = 0; i < AddUpdates.get(0).size(); i++) {
            final int finalI = i;
            FirebaseFirestore.getInstance().collection("users/" + userID + "/products")
                    .add(AddUpdates.get(i))
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            AddUpdates.remove(finalI);
                            success[0] = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            success[0] = false;
                        }
                    });
            if(success[0])
                i--;
            else
                return false;
        }
        return true;
    }
}
