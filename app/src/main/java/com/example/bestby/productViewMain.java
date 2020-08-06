package com.example.bestby;

import android.media.Image;

public class productViewMain {
    private String productName;
    private String displayDate;
    private boolean check;

    public productViewMain(String productName, String displayDate, boolean check){

        this.productName = productName;
        this.displayDate = displayDate;
        this.check = check;
    }

    public String getName() { return productName; }
    public String getDisplayDate() { return displayDate; }
    public boolean getCheck() { return check; }
}
