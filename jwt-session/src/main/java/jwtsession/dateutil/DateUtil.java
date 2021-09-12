package jwtsession.dateutil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static LocalDateTime setExpireTime(long minutes){
        return LocalDateTime.now().plusMinutes(minutes);
    }
    public static Date setExpiraryDaysToRefreshToken(int days){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static String getTimeFormat(LocalDateTime localDateTime){
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        //new Date(localDateTime);
        String time = dateFormat.format(localDateTime);
     return time;
    }

    public static Date todayDate(){
        TimeZone tz = TimeZone.getTimeZone("IST");
        Calendar calendar = Calendar.getInstance(tz);
        Date date=calendar.getTime();
        return date;
    }

    public static Date addMinutes(int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public static Date addDays(int days){
        Date date = new Date();
        TimeZone tz = TimeZone.getTimeZone("IST");
        Calendar calendar = Calendar.getInstance(tz);
        calendar.setTimeZone(tz);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.add(Calendar.DATE,1);
        date = calendar.getTime();
        return calendar.getTime();
    }

    public static Date addMonths(int numberOfMonths){
        TimeZone tz = TimeZone.getTimeZone("IST");
        Calendar calendar = Calendar.getInstance(tz);
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, numberOfMonths);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }

    public static String dateFormat(Date date){
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

}
