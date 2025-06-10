package org.vg.markusbro.core;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static double parseDouble(String s) {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        final char decimalSeparator = symbols.getDecimalSeparator();
        return Double.parseDouble(s
                .replace('.', decimalSeparator)
                .replace(',', decimalSeparator));
    }

    public static SimpleDateFormat getDateTimeFormatter() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public static SimpleDateFormat getDateFormatter() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    public static DateTimeFormatter getFileNameFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
    }

    public static Date getDateLastWeek() {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -7);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static Date getDateLastMonth() {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }
}
