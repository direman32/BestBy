package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import static com.example.bestby.staticValues.USER_ID;

public class RemoveProductPage extends AppCompatActivity {

    private FirebaseFirestore fStoreRef;
    private EditText amountField;
    private String userID;
    private String documentID;
    private View progressOverlay;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_product_page);
        fStoreRef = FirebaseFirestore.getInstance();
        amountField = findViewById(R.id.AmountExpired);
        userID = getIntent().getStringExtra(USER_ID);
        progressOverlay = findViewById(R.id.progressIconOverlay);
        confirmButton = findViewById(R.id.RemoveProductConfirm);
        progressOverlay.setVisibility(View.INVISIBLE);
        documentID = getIntent().getStringExtra("documentID");
    }

    public void RemoveProduct(View view) {
        setVisible();
        final FirebaseFirestore db = fStoreRef;
        final boolean[] failed = {false};

        db.collection("users/" + userID + "/products").document("" + documentID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> map = document.getData();
                                map.put("numberRemoved", amountField.getText().toString());
                                db.collection("users/" + userID +"/removedProducts")
                                        .add(map);

                                db.collection("users/" + userID + "/products").document(""+ documentID)
                                        .delete();
                                finish();
                                setInvisible();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                        } else {
                            Log.d("Document Ref", "get failed with ", task.getException());
                        }
                    }
                });
    }
    public void setInvisible() {
        progressOverlay.setVisibility(View.INVISIBLE);
        confirmButton.setClickable(true);
        amountField.setClickable(true);
    }
    public void setVisible() {
        progressOverlay.setVisibility(View.VISIBLE);
        confirmButton.setClickable(false);
        amountField.setClickable(false);
    }
}