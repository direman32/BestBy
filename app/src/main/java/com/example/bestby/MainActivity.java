package com.example.bestby;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private SearchView productSearch;
    private long closestDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        shopName = findViewById(R.id.ShopName);
        productsView = findViewById(R.id.productsView);
        progressOverlay = findViewById(R.id.progressIconOverlayMain);
        addProductButton = findViewById(R.id.addNewItem);
        removedProductButton = findViewById(R.id.button2);
        productSearch = findViewById(R.id.productSearch);

        productSearch.setIconifiedByDefault(false);
        productSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // do something on text submit
                ShowProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // do something when text changes
                if(newText.equals("")) {
                    ShowProducts(newText);
                }
                return false;
            }
        });

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
        ShowProducts("");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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

    public void ShowProducts(final String query) {
        FirebaseFirestore.getInstance()
                .collection("users/" + userID + "/products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            FillListView(myListOfDocuments, query);
                        }
                    }
                });
    }

    public void FillListView(List<DocumentSnapshot> myListOfDocuments, String query) {
        ArrayList<productViewMain> productViewMains = new ArrayList<>();

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your array as a third parameter.
        ArrayAdapter<productViewMain> arrayAdapter;
        arrayAdapter = new MainCustomArrayAdpater(
                this,
                R.layout.main_list_item,
                productViewMains);


        myListOfDocuments = SortByDate(myListOfDocuments);
        boolean closestSet = false;
        for(int i = 0; i < myListOfDocuments.size(); i++) {
            boolean check = checkDate(myListOfDocuments.get(i).get(staticValues.PRODUCT_DATE_KEY).toString());
            if(searchCheck(query,  myListOfDocuments.get(i).get(staticValues.PRODUCT_NAME_KEY).toString().trim())) {
                productViewMains.add(new productViewMain(
                        myListOfDocuments.get(i).get(staticValues.PRODUCT_NAME_KEY).toString().trim(),
                        myListOfDocuments.get(i).get(staticValues.PRODUCT_DATE_DISPLAY_KEY).toString().trim(),
                        check));
                if (!check && !closestSet) {
                    closestDate = Long.parseLong(myListOfDocuments.get(i).get(staticValues.PRODUCT_DATE_KEY).toString());
                    closestDate = closestDate + (1000 * 3600 * 12);
                    closestSet = true;
                    alarmSetup();
                }
            }
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

    private boolean searchCheck(String query, String productName) {
        String[] querySplit = query.split(" ");
        for(String queryPart: querySplit) {
            if(!productName.toLowerCase().contains(queryPart.toLowerCase()))
                return false;
        }
        return true;
    }

    private boolean checkDate(String date) {
        Long expiryDate = Long.parseLong(date);
        Calendar expiryCal = Calendar.getInstance();
        expiryCal.setTimeInMillis(expiryDate);

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(System.currentTimeMillis());
        nowCal.add(Calendar.DAY_OF_MONTH, staticValues.EXPIRY_DATE_NOTIFIER);

        return expiryCal.before(nowCal);
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


    private void alarmSetup() {
        Intent intent = new Intent(MainActivity.this, BroadcastManager.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);


        alarmManager.set(AlarmManager.RTC_WAKEUP,
                closestDate,
                pendingIntent);
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
