package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddProduct extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    TextView product;
    TextView dateText;
    DatePickerDialog dialog;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        dateText = findViewById(R.id.dateTextView);
        product = findViewById(R.id.NameOfProduct);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
    }

    public void ChooseDate(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        dialog = datePickerDialog;

//        DateDialog dateDialog = new DateDialog();
//        dateDialog.show(getSupportFragmentManager(), "date dialog");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = "Expiry Date: " + dayOfMonth + "/" + month + "/" + year;
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

    public void AddProductToDataBaseSuccessful(View view) {
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
        user.put("product", product.getText().toString());
        user.put("expiryDate", timestamp);
        user.put("expiryDateDisplay", date);

// Add a new document with a generated ID
        db.collection("users/" + userID +"/products")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        System.out.println("DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Error adding document: "+ e.getMessage());
                    }
                });

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //intent.putExtra("Date",convertDate(day,month,year));
        startActivity(intent);
        finish();
    }
}