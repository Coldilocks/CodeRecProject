package utils;

import org.apache.commons.lang.time.StopWatch;

public class TimeUtil {

    private static StopWatch stopWatch = new StopWatch();

    public static void startTimer(){
        stopWatch.start();
    }

    public static String stopAndGetTime(){
        stopWatch.stop();
        String time = String.valueOf(stopWatch.getTime());
        stopWatch.reset();
        return time;
    }

    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;
        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;
        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "d");
        }
        if (hour > 0) {
            sb.append(hour + "h");
        }
        if (minute > 0) {
            sb.append(minute + "m");
        }
        if (second > 0) {
            sb.append(second + "s");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + "ms");
        }
        return sb.toString();
    }
}
