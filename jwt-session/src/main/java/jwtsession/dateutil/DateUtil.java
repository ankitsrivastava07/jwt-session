package jwtsession.dateutil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static LocalDateTime addDays(long days){
        LocalDateTime localDateTime= LocalDateTime.now();
        localDateTime.plusDays(days);
        return localDateTime;
    }

    public static LocalDateTime getExpireDate(long days){
        return addDays(days);
    }

    public static LocalDateTime setExpireTime(long minutes){
        return LocalDateTime.now().plusMinutes(minutes);
    }

    public static Date setExpiraryDaysToRefreshToken(int days){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static String getTimeFormat(LocalDateTime localDateTime){
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        //new Date(localDateTime);
        String time = dateFormat.format(localDateTime);
     return time;
    }

    public static LocalDateTime getOneMonthBeforeFromToday(long months){
        LocalDateTime oneMonthBefore=LocalDateTime.now().minusMonths(months);
        LocalDateTime today=LocalDateTime.now();
        return oneMonthBefore;
    }

}
