package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.bestby.staticValues.REMOVED_PRODUCTS;

public class ItemEdit extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private String userID;
    private int productPosition;
    private String documentID;
    private EditText productName;
    private TextView dateText;
    private TextView offlineList;
    private String originalName;
    private String originalDate;
    private long originalTimeStamp;
    private long newTimeStamp;
    private Calendar calendar;
    private FirebaseFirestore fStoreRef;
    private String newDateDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);

        fStoreRef = FirebaseFirestore.getInstance();
        productName = findViewById(R.id.editProductName);
        dateText = findViewById(R.id.editDate);
        offlineList = findViewById(R.id.offlineList);
        productName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    if(!productName.getText().toString().equals(originalName))
                        productName.setTextColor(Color.RED);
                    else
                        productName.setTextColor(Color.BLACK);
                }
            }
        });
        productName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    clearFocus(v);
                    handled = true;
                }
                return handled;
            }
        });

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
                            productName.setText(myListOfDocuments.get(productPosition).getString(staticValues.PRODUCT_NAME_KEY));
                            originalName = productName.getText().toString();
                            originalDate = myListOfDocuments.get(productPosition).getString(staticValues.PRODUCT_DATE_DISPLAY_KEY);
                            dateText.setText("Expiry Date: " + originalDate);
                            originalTimeStamp = myListOfDocuments.get(productPosition).getLong(staticValues.PRODUCT_DATE_KEY);
                        }
                    }
                });
    }

    public void RemoveItemDialog(View view) {
        clearFocus(view);
        RemoveItemDialog rid = new RemoveItemDialog(ItemEdit.this, userID, productPosition, fStoreRef, offlineList);
        rid.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rid.show();
        rid.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface arg0) {
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    public static void finishActivity(Activity item) {
        item.finish();
    }

    public void Update(View view) {
        clearFocus(view);
        if(productName.getText().toString().equals(originalName) && (originalDate.equals(newDateDisplay) || newDateDisplay == null)) {
            Toast.makeText(ItemEdit.this, "Nothing has been changed",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            if(newDateDisplay == null) {
                newDateDisplay = originalDate;
                newTimeStamp = originalTimeStamp;
            }
            final FirebaseFirestore fStore = fStoreRef;
            final DocumentReference contact = fStore.collection("users/" + userID + "/products").document(documentID);
            contact.update(staticValues.PRODUCT_NAME_KEY, productName.getText().toString().trim());
            contact.update(staticValues.PRODUCT_DATE_KEY, newTimeStamp);
            contact.update(staticValues.PRODUCT_DATE_DISPLAY_KEY, newDateDisplay)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ItemEdit.this, "Updated Successfully",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra(REMOVED_PRODUCTS, (Serializable) setUpRemovedProductList(fStore));
                            finish();
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ItemEdit.this, "Could Not Update",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            fStoreRef = fStore;
        }
    }

    //TODO give access to removeItemDialog
    private List<String> setUpRemovedProductList(FirebaseFirestore fStore) {
        //Make Serialized
        fStore.collection("users/" + userID + "/products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        StringBuilder documentDetails = new StringBuilder();
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            for(int i = 0; i < myListOfDocuments.size(); i++) {
                                documentDetails.append(String.format("%-17s %s",
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if(month+1 < 10)
            newDateDisplay= dayOfMonth + "-0" + (month + 1) + "-" + year;
        else
            newDateDisplay= dayOfMonth + "-" + (month + 1) + "-" + year;
        String date = "Expiry Date: " + newDateDisplay;
        if(!(newDateDisplay).equals(originalDate) && !newDateDisplay.equals(""))
            dateText.setTextColor(Color.RED);
        else
            dateText.setTextColor(Color.BLACK);
        dateText.setText(date);
        DateFormat format = new SimpleDateFormat("dd-mm-yyyy");
        try {
            Date dateTime = format.parse(newDateDisplay);
            newTimeStamp = dateTime.getTime();
        } catch (ParseException e) {
            Log.e("log", e.getMessage(), e);
        }
    }

    public void EditDate(View view) {
        clearFocus(view);
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(originalTimeStamp);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
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

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
    public void clearFocus (View view) {
        View current = getCurrentFocus();
        if (current != null) current.clearFocus();
        hideSoftKeyboard(ItemEdit.this, view);
    }
}