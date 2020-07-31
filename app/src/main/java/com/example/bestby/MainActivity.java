package com.example.bestby;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.example.bestby.staticValues.REMOVED_PRODUCTS;
import static com.example.bestby.staticValues.UPDATER;
import static com.example.bestby.staticValues.USER_ID;

public class MainActivity extends AppCompatActivity {

    private TextView shopName;
    private ListView productsView;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private Updater updateHandler;
    private List<String> removedProducts;
    private View progressOverlay;
    private Button addProductButton;
    private Button removedProductButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        shopName = findViewById(R.id.ShopName);
        productsView = findViewById(R.id.productsView);
        progressOverlay = findViewById(R.id.progressIconOverlayMain);
        addProductButton = findViewById(R.id.addNewItem);
        removedProductButton = findViewById(R.id.button2);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                shopName.setText(documentSnapshot.getString("Shop"));
            }
        });

        updateHandler = (Updater) getIntent().getSerializableExtra("updater");
        removedProducts = (List<String>) getIntent().getSerializableExtra(REMOVED_PRODUCTS);

        setVisible();
        ShowProducts();
    }

    public void LogOut(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    public void AddProduct(View view) {
        if(updateHandler != null)
            updateHandler.UpdateNewProducts();
        Intent intent = new Intent(getApplicationContext(), AddProduct.class);
        intent.putExtra(USER_ID,userID);
        intent.putExtra(UPDATER,updateHandler);
        //finish();
        startActivity(intent);
    }

    public void RemovedProductsPage(View view) {
        Intent intent = new Intent(getApplicationContext(), RemovedProducts.class);
        intent.putExtra(USER_ID,userID);
        //finish();
        startActivity(intent);
    }

    public void WriteProduct(String Product) {
        getFilesDir();
    }

    public void ShowProducts() {
        FirebaseFirestore.getInstance()
                .collection("users/" + userID + "/products")
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
            String formattedText = String.format("%-17s %s",
                    myListOfDocuments.get(i).get(staticValues.PRODUCT_NAME_KEY).toString().trim(),
                    myListOfDocuments.get(i).get(staticValues.PRODUCT_DATE_DISPLAY_KEY).toString().trim());
//            if(removedProducts != null) {
//                if (!removedProductsCheck(formattedText)) {
//                    documentDetails.add(formattedText);
//                }
//            }
//            else
                documentDetails.add(formattedText);
        }
        productsView.setAdapter(arrayAdapter);

        productsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ItemEdit.class);
                intent.putExtra("userID", userID);
                intent.putExtra("productPosition", position);
                finish();
                startActivity(intent);
            }
        });

        setInvisible();
    }

    //TODO If coming back from removedProductsPage
    private boolean removedProductsCheck(String dataCheck) {
        for(String displayText: removedProducts) {
            if(displayText.equals(dataCheck)) {
                return true;
            }
        }
        return false;
    }

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

    public void setInvisible() {
        progressOverlay.setVisibility(View.INVISIBLE);
        addProductButton.setClickable(true);
        removedProductButton.setClickable(true);
    }
    public void setVisible() {
        progressOverlay.setVisibility(View.VISIBLE);
        addProductButton.setClickable(false);
        removedProductButton.setClickable(false);
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
        //TODO add in are you sure you want to logout?
//        Intent intent = new Intent(getApplicationContext(), Login.class);
//        intent.putExtra("logOut", true);
//        startActivity(intent);
    }
}
