package com.example.cloneinstagram.helper;

import java.text.SimpleDateFormat;

public class DateUtil {

    public static String actualDate(){

        long date = System.currentTimeMillis();
        SimpleDateFormat mDateFormat = new SimpleDateFormat("d/M/yyyy");
        String dateString = mDateFormat.format(date);

        return dateString;
    }
}
