package com.example.livestream_apd.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Slf4j
public class TimeUtil {
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(UTC_ZONE);
    }

    public static LocalDateTime nowVietNam() {
        return LocalDateTime.now(VIETNAM_ZONE);
    }

    public static LocalDateTime nowSystem(){
        return LocalDateTime.now();
    }

    public static LocalDateTime toUtc(LocalDateTime localDateTime){
        if (localDateTime == null )
            return null;
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.withZoneSameInstant(UTC_ZONE).toLocalDateTime();
    }

    public static LocalDateTime fromUtcToVietNam(LocalDateTime localDateTime){
        if (localDateTime == null )
            return null;
        ZonedDateTime zonedDateTime = localDateTime.atZone(UTC_ZONE);
        return zonedDateTime.withZoneSameInstant(VIETNAM_ZONE).toLocalDateTime();
    }

    public static LocalDateTime fromUtcToSystem (LocalDateTime localDateTime){
        if (localDateTime == null )
            return null;
        ZonedDateTime zonedDateTime = localDateTime.atZone(UTC_ZONE);
        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static boolean isAfterNowUtc (LocalDateTime localDateTime){
        if (localDateTime == null )
            return false;
        return localDateTime.isAfter(nowUtc());
    }

    public static boolean isBeforeNowUtc (LocalDateTime localDateTime){
        if (localDateTime == null )
            return false;
        return localDateTime.isBefore(nowUtc());
    }

    public static boolean isExpire (LocalDateTime localDateTime){
        if (localDateTime == null )
            return false;
        return isBeforeNowUtc(nowUtc());
    }

    public static LocalDateTime nowUtcPlusSeconds(long seconds){
        return nowUtc().plusSeconds(seconds);
    }

    public static LocalDateTime nowUtcPlusMinutes(long minutes){
        return nowUtc().plusMinutes(minutes);
    }

    public static LocalDateTime nowUtcPlusHours(long hours){
        return nowUtc().plusHours(hours);
    }

    public static LocalDateTime nowUtcPlusDays(long days){
        return nowUtc().plusDays(days);
    }

   public static Instant nowInstant(){
        return Instant.now();
   }

   public static LocalDateTime intantToUtcLocalDateTime(Instant instant){
        if (instant == null )
            return null;
        return LocalDateTime.ofInstant(instant, UTC_ZONE);
   }

   public static Instant utcLocalDateTimeToInstant (LocalDateTime localDateTime){
        if (localDateTime == null )
           return null;
        return localDateTime.atZone(UTC_ZONE).toInstant();
   }

   public static String formatUtc (LocalDateTime localDateTime){
        if (localDateTime == null )
            return null;
        return localDateTime.format(DEFAULT_FORMATTER) + "UTC";
   }

   public static String formatVietNam (LocalDateTime localDateTime){
        if (localDateTime == null )
            return null;
        LocalDateTime vietnamTime = fromUtcToVietNam(localDateTime);
        return vietnamTime.format(DEFAULT_FORMATTER) + " +07:00";
   }

   public static long getDifferenceInSeconds (LocalDateTime start, LocalDateTime end){
        if (start == null || end == null){
            return 0;
        }
        return Duration.between(start, end).toSeconds();
   }

   public static long getDifferenceInMinutes (LocalDateTime start, LocalDateTime end){
        if (start == null || end == null){
            return 0;
        }
        return Duration.between(start, end).toMinutes();
   }

   public static long getRemainingTimeInSecond (LocalDateTime expiryTime){
        if (expiryTime == null){
            return 0;
        }
        long remaining = getDifferenceInSeconds(nowUtc(), expiryTime);
        return Math.max(0, remaining);
   }



   public static void debugTime(){
        System.out.println("TimeDebug");
        System.out.println("Debug System Time: " + nowSystem().format(DEFAULT_FORMATTER));
        System.out.println("Debug UTC Time: " + nowUtc().format(DEFAULT_FORMATTER));
        System.out.println("Viet Nam Time: " + nowVietNam().format(DEFAULT_FORMATTER));
        System.out.println("System Time Zone: " + ZoneId.systemDefault());
   }

    public static void verifyTimezoneSetup() {
        System.out.println("=== TIMEZONE VERIFICATION ===");
        System.out.println("JVM Default Timezone: " + java.util.TimeZone.getDefault().getID());
        System.out.println("ZoneId System Default: " + ZoneId.systemDefault());

        LocalDateTime systemTime = LocalDateTime.now();
        LocalDateTime utcTime = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime vietnamTime = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        System.out.println("System LocalDateTime.now(): " + systemTime.format(DEFAULT_FORMATTER));
        System.out.println("UTC LocalDateTime.now(): " + utcTime.format(DEFAULT_FORMATTER));
        System.out.println("Vietnam LocalDateTime.now(): " + vietnamTime.format(DEFAULT_FORMATTER));

        System.out.println("TimeUtil.nowUtc(): " + nowUtc().format(DEFAULT_FORMATTER));
        System.out.println("TimeUtil.nowVietNam(): " + nowVietNam().format(DEFAULT_FORMATTER));
        System.out.println("============================");
    }


}
