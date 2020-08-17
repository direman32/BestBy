package com.example.bestby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RemovedProducts extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private ListView removedProductsView;
    private String userID;
    private View progressOverlay;
    private List<DocumentSnapshot> myProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_removed_products);

        fAuth = FirebaseAuth.getInstance();
        removedProductsView = findViewById(R.id.removedProductsList);
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        progressOverlay = findViewById(R.id.progressIconOverlayRemovedProducts);
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
                            myProducts = myListOfDocuments;
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
            documentDetails.add(String.format("%s %s\n%s", myListOfDocuments.get(i).get(staticValues.PRODUCT_NAME_KEY).toString().trim(),
                    "x" + myListOfDocuments.get(i).get("numberRemoved").toString().trim(),
                    myListOfDocuments.get(i).get(staticValues.PRODUCT_DATE_DISPLAY_KEY).toString().trim()));
        }
        removedProductsView.setAdapter(arrayAdapter);

        removedProductsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        setInvisible();
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


    public void GenerateReport(View view) {
//        startActivity(new Intent(getApplicationContext(), Excel.class));
        Workbook wb=new HSSFWorkbook();
        Cell cell=null;
        CellStyle cellStyle=wb.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
        //cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        //Now we are creating sheet
        Sheet sheet=null;
        sheet = wb.createSheet("Removed Products");
        //Now column and row


        myProducts = SortByDate(myProducts);
        for(int i = 0; i < myProducts.size(); i++) {
            Row row = sheet.createRow(i);

            cell = row.createCell(0);
            cell.setCellValue(myProducts.get(i).get(staticValues.PRODUCT_NAME_KEY).toString().trim());
            cell.setCellStyle(cellStyle);

            cell = row.createCell(1);
            cell.setCellValue(myProducts.get(i).get("numberRemoved").toString().trim());
            cell.setCellStyle(cellStyle);

            cell = row.createCell(2);
            cell.setCellValue(myProducts.get(i).get(staticValues.PRODUCT_DATE_DISPLAY_KEY).toString().trim());
            cell.setCellStyle(cellStyle);
        }

        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 2000);
        sheet.setColumnWidth(2, 3000);

        File file = new File(getExternalFilesDir(null),"WeeklyReport.xls");
        FileOutputStream outputStream =null;

        try {
            outputStream=new FileOutputStream(file);
            wb.write(outputStream);
            Toast.makeText(getApplicationContext(),"OK",Toast.LENGTH_LONG).show();
        } catch (java.io.IOException e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"NO OK",Toast.LENGTH_LONG).show();
            try {
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }



    public void setInvisible() {
        progressOverlay.setVisibility(View.INVISIBLE);
    }
    public void setVisible() {
        progressOverlay.setVisibility(View.VISIBLE);
    }
}