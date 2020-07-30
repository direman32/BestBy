package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.bestby.staticValues.UPDATER;
import static com.example.bestby.staticValues.USER_ID;

public class AddProduct extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView product;
    private TextView dateText;
    private DatePickerDialog dialog;
    private String userID;
    private Updater updateHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        dateText = findViewById(R.id.dateTextView);
        product = findViewById(R.id.NameOfProduct);

        Intent intent = getIntent();
        userID = intent.getStringExtra(USER_ID);
        updateHandler = (Updater) intent.getSerializableExtra(UPDATER);
    }

    public void ChooseDate(View view) {
        hideSoftKeyboard(AddProduct.this, view);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        dialog = datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = "Expiry Date: " + dayOfMonth + "-" + (month + 1) + "-" + year;
        dateText.setText(date);
    }

    public static String convertDateFromString(String dateInMilliseconds,String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }

    public static String convertDate(int dayOfMonth, int month, int year) {
        return dayOfMonth + "/" + month + "/" + year;
    }

    public String GetExpiryDate() {
        return dateText.getText().toString();
    }

    public void addProductChecks(View view) {
        String productName = product.getText().toString().trim();
        String date = dateText.getText().toString().trim();

        if(TextUtils.isEmpty(productName)) {
            product.setError("Product Name Required");
            return;
        }
        if(TextUtils.isEmpty(date) || date.equals("Date Required!")) {
            dateText.setText("Date Required!");
            return;
        }
        AddProductToDataBaseSuccessful();
    }

    public void AddProductToDataBaseSuccessful() {
        final boolean[] failed = {false};

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        int day =  dialog.getDatePicker().getDayOfMonth();
        int month = dialog.getDatePicker().getMonth();
        int year = dialog.getDatePicker().getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long timestamp = calendar.getTimeInMillis();

        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd-MM-yyyy", calendar).toString();
        user.put(staticValues.PRODUCT_NAME_KEY, product.getText().toString().trim());
        user.put(staticValues.PRODUCT_DATE_KEY, timestamp);
        user.put(staticValues.PRODUCT_DATE_DISPLAY_KEY, date);

        // Add a new document with a generated ID
        db.collection("users/" + userID +"/products")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddProduct.this, "Product Added Successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddProduct.this, "Could not add product at this time",
                                Toast.LENGTH_SHORT).show();
                        failed[0] = true;
                    }
                });

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent = failureForUpdate(intent, failed[0], user);
        startActivity(intent);
        finish();
    }

    private Intent failureForUpdate(Intent intent, boolean failed, Map<String, Object> userDetails) {
        if(failed) {
            if(updateHandler != null) {
                updateHandler.addNewProduct(userDetails);
                intent.putExtra(UPDATER, updateHandler);
            }
            else {
                Updater updater =new Updater(userID);
                updateHandler.addNewProduct(userDetails);
                intent.putExtra(UPDATER, updater);
            }
            return intent;
        }
        else
            return intent;
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}