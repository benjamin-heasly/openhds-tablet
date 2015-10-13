package org.openhds.mobile.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ben on 10/12/15.
 */
public class DateUtils {

    public static String formatDateTimeIso(Calendar calendar) {
        // move calendar to UTC
        Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(calendar.getTimeInMillis());

        // build an ISO date time string
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        String zoneless = simpleDateFormat.format(utc.getTime());
        return zoneless + "Z[UTC]";
    }

    // only get the date, truncate the time
    public static Calendar parseDateIso(String string) {
        if (null == string) {
            return Calendar.getInstance();
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        try {
            calendar.setTime(simpleDateFormat.parse(string));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendar;
    }

}
