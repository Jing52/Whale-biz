package com.whale.framework.common.utils;

import com.whale.framework.common.dto.exception.BizServiceException;
import com.whale.framework.common.dto.response.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j(topic = "rpc-common")
public class DateUtil {

    public static final String YYYY_M_D = "yyyy-M-d";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD_HH = "yyyy-MM-dd HH";
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddhhmmssSSS";
    public static final String YYYY_MM_DD_HH_MM_SSSSS = "yyyy-MM-dd HH:mm:ss SSS";
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String HH_mm = "HH:mm";
    public static final String M_YUE_DD_RI = "M月dd日";

    public static final String YYYY_NIAN_MM_YUE_DD_RI_HH_MM = "yyyy年MM月dd日 HH:mm";
    public static final String MM_YUE_DD_RI_HH_MM = "MM月dd日HH时mm分";
    public static final String YYYY_NIAN_MM_YUE_DD_RI = "yyyy年MM月dd日";
    public static final String DDMMYY_HHMMSS = "dd/MM/yyyy HH:mm:ss";
    public static final String MMDDYY = "MM/dd/yyyy";
    public static final String INIT_DATE = "1970-1-1";
    public static final String INIT_DATE_ZERO = "1970-01-01";
    public static final String HH_mm_ss = "HH:mm:ss";
    public static final String HH_mm_ss_SSS = "HH:mm:ss SSS";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";

    public static long getDateDiffByDay(String time1, String time2) {
        long quot = 0;
        SimpleDateFormat ft = new SimpleDateFormat(YYYY_MM_DD);
        try {
            Date date1 = ft.parse(time1);
            Date date2 = ft.parse(time2);
            quot = date1.getTime() - date2.getTime();
            quot = quot / 1000 / 60 / 60 / 24;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return quot;
    }

    public static long getDateDiffByYear(String departureDate, String birthday) {
        Calendar calDeparture = Calendar.getInstance();
        calDeparture.setTime(DateUtil.parseDate(departureDate));

        Calendar calBirth = Calendar.getInstance();
        calBirth.setTime(DateUtil.parseDate(birthday));
        if (calDeparture.before(calBirth)) {
            throw new IllegalArgumentException("还未出生!");
        }

        int yearDeparture = calDeparture.get(Calendar.YEAR);
        int monthDeparture = calDeparture.get(Calendar.MONTH) + 1;
        int dayOfMonthDeparture = calDeparture.get(Calendar.DAY_OF_MONTH);

        int yearBirth = calBirth.get(Calendar.YEAR);
        int monthBirth = calBirth.get(Calendar.MONTH) + 1;
        int dayOfMonthBirth = calBirth.get(Calendar.DAY_OF_MONTH);

        int age = yearDeparture - yearBirth;

        if (monthDeparture <= monthBirth) {
            if (monthDeparture == monthBirth) {
                if (dayOfMonthDeparture < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        }
        return age;
    }


    public static Date getDateAfterMinutes(long duration) {
        long curr = System.currentTimeMillis();
        curr = curr + duration * 60 * 1000;
        return new Date(curr);
    }

    public static Date getDateBeforeMinutes(long duration) {
        long curr = System.currentTimeMillis();
        curr = curr - duration * 60 * 1000;
        return new Date(curr);
    }

    /**
     * 当前时间之前几分钟
     *
     * @param date
     * @param duration
     * @return 时间串
     */
    public static String getDateStrBeforeDateMinutes(Date date, long duration) {
        long curr = date.getTime();
        curr = curr - duration * 60 * 1000;
        SimpleDateFormat fmt = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        return fmt.format(new Date(curr));
    }

    public static String parseLocalDateTime(LocalDateTime localDateTime, String format) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return df.format(localDateTime);
    }

    /**
     * 当前时间之后n分钟
     *
     * @param date
     * @param duration
     * @return
     */
    public static Date getDateAfterDateMinutes(Date date, long duration) {
        long curr = date.getTime();
        curr = curr + duration * 60 * 1000;
        return new Date(curr);
    }

    /**
     * 当前时间之后n秒钟
     *
     * @param date
     * @param duration
     * @return
     */
    public static Date getDateAfterDateSecond(Date date, long duration) {
        long curr = date.getTime();
        curr = curr + duration * 1000;
        return new Date(curr);
    }


    /**
     * 当前时间之后的几天
     *
     * @param date
     * @param
     * @return
     */
    public static Date getDateAfterDateDays(Date date, long day) {
        if (date == null) {
            return null;
        }
        long curr = date.getTime();
        curr = curr + day * 24 * 60 * 60 * 1000;
        return new Date(curr);
    }


    /**
     * 两个时间相差多少秒
     *
     * @param time1 时间参数 1 格式：1990-01-01 12:00:00
     * @param time2 时间参数 2 格式：2009-01-01 12:00:00
     * @return String 返回值为：秒
     */
    public static long getDateDiffBySecond(String time1, String time2) {
        long quot = 0;
        SimpleDateFormat ft = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        try {
            Date date1 = ft.parse(time1);
            Date date2 = ft.parse(time2);
            quot = date1.getTime() - date2.getTime();
            quot = quot / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return quot;
    }

    /**
     * 两个时间相差多少毫秒
     *
     * @param time1 时间参数 1 格式：Date
     * @param time2 时间参数 2 格式：Date
     * @return String 返回值为：毫秒
     */
    public static long getDateDiffByMillisecond(Date time1, Date time2) {
        long quot = 0;
        quot = time1.getTime() - time2.getTime();
        return quot;
    }

    /**
     * 两个时间相差多少秒
     *
     * @param time1 时间参数 1 格式：Date
     * @param time2 时间参数 2 格式：Date
     * @return String 返回值为：秒
     */
    public static int getDateDiffBySecond(Date time1, Date time2) {
        long quot = 0;
        quot = time1.getTime() - time2.getTime();
        quot = quot / 1000;
        return (int) quot;
    }

    /**
     * 两个时间相差多少分钟
     *
     * @param time1 时间参数 1 格式：1990-01-01 12:00:00
     * @param time2 时间参数 2 格式：2009-01-01 12:00:00
     * @return String 返回值为：分钟
     */
    public static long getDateDiffByMin(String time1, String time2) {
        long quot = 0;
        SimpleDateFormat ft = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        try {
            Date date1 = ft.parse(time1);
            Date date2 = ft.parse(time2);
            quot = date1.getTime() - date2.getTime();
            quot = quot / 1000 / 60;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return quot;
    }

    public static int getDateDiffByHour(Date time1, Date time2) {
        long quot = 0;
        quot = time1.getTime() - time2.getTime();
        quot = quot / 1000 / 60 / 60;
        return (int) quot;
    }

    public static int getDateDiffByDay(Date time1, Date time2) {
        long quot = 0;
        quot = time1.getTime() - time2.getTime();
        quot = quot / 1000 / 60 / 60 / 24;
        return (int) quot;
    }

    /**
     * 两个时间相差多少分钟
     *
     * @param time1 时间参数 1 格式：Date
     * @param time2 时间参数 2 格式：Date
     * @return String 返回值为：分钟
     */
    public static int getDateDiffByMin(Date time1, Date time2) {
        long quot = 0;
        quot = time1.getTime() - time2.getTime();
        quot = quot / 1000 / 60;
        return (int) quot;
    }


    public static Date getDateDiffNDay(String strDate, int n) {
        SimpleDateFormat ft = new SimpleDateFormat(YYYY_MM_DD);
        try {
            Date dDate = ft.parse(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dDate);
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + n);
            return calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date parseDate(String sDate, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        if (StringUtils.isNotEmpty(sDate)) {
            try {
                return format.parse(sDate);
            } catch (ParseException e) {
                e.printStackTrace(); // To change body of catch statement use
                // File | Settings | File Templates.
                return null;
            }
        }
        return null;
    }

    public static Date parseCnDate(String sDate) {
        return parseDate(sDate, YYYY_NIAN_MM_YUE_DD_RI);
    }

    public static Date parseDate(String sDate) {
        return parseDate(sDate, YYYY_MM_DD);
    }

    public static Date parseDateYYYYMM(String sDate) {
        return parseDate(sDate, YYYY_MM);
    }

    public static Date parseDate2YYYYMMDD(Date date) {
        return parseDate(formatDate(date, YYYY_MM_DD), YYYY_MM_DD);
    }

    public static Date parseCnHmDate(String sDate) {
        return parseDate(sDate, YYYY_MM_DD_HH_MM_SS);
    }

    public static Date parseHhDate(String sDate) {
        return parseDate(sDate, YYYY_MM_DD_HH);
    }

    public static String formatDateByFormat(Date sDate, String Format) {
        return formatDate(sDate, Format);
    }

    public static String formatDateYYYYMMDD(Date sDate) {
        return formatDate(sDate, YYYYMMDD);
    }

    public static String formatDateYYYY_MM(Date sDate) {
        return formatDate(sDate, YYYY_MM);
    }

    public static String formatDate(Date sDate) {
        return formatDate(sDate, YYYY_MM_DD);
    }

    public static String formatTime(Date sDate) {
        return formatDate(sDate, HH_mm_ss);
    }

    public static String formatTimeMillisecond(Date sDate) {
        return formatDate(sDate, HH_mm_ss_SSS);
    }

    public static String formatCnDate(Date sDate) {
        return formatDate(sDate, YYYY_NIAN_MM_YUE_DD_RI);
    }

    public static String formatCnHmDate(Date sDate) {
        return formatDate(sDate, YYYY_MM_DD_HH_MM_SS);
    }

    public static String formatHhMmDate(Date sDate) {
        return formatDate(sDate, YYYY_MM_DD_HH_MM);
    }

    public static String formatHhDate(Date sDate) {
        return formatDate(sDate, YYYY_MM_DD_HH);
    }

    public static String formatCnHmDateS(Date sDate) {
        return formatDate(sDate, YYYY_MM_DD_HH_MM_SSSSS);
    }

    public static String formatHmDate(Date sDate) {
        return formatDate(sDate, HH_mm);
    }

    public static String formatDate(Date sDate, String formatStr) {
        if (sDate == null) {
            return "";
        }
        DateFormat format = new SimpleDateFormat(formatStr);
        return format.format(sDate);
    }

    public static String formatDate(Long sDate, String formatStr) {
        if (sDate == null) {
            return "";
        }
        Date date = new Date(sDate);
        DateFormat format = new SimpleDateFormat(formatStr);
        return format.format(date);
    }

    public static String defaultFormatDate(Date sDate) {
        return formatDate(sDate, YYYY_MM_DD_HH_MM_SS);
    }

    public static boolean isValidDate(String dateStr, String pattern) {
        return formatDate(parseDate(dateStr, pattern), pattern).equals(dateStr);
    }

    public static Date addMinutes(Date date, int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    public static String formatDateHour(Date sDate) {
        return formatDate(sDate, YYYY_MM_DD_HH_MM);
    }

    public static Date addHours(Date date, int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    public static Date addDays(Date date, int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    public static Date addMonths(Date date, int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    public static Date add(Date date, int calendarField, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    public static String getStringCurrentDateTime() {
        Calendar rightNow = Calendar.getInstance();
        int intYear = rightNow.get(Calendar.YEAR);

        int intMonth = rightNow.get(Calendar.MONTH) + 1;
        String strMonth = null;
        if (intMonth < 10) {
            strMonth = "0" + intMonth;
        } else {
            strMonth = "" + intMonth;
        }

        int intDate = rightNow.get(Calendar.DATE);
        String strDate = null;
        if (intDate < 10) {
            strDate = "0" + intDate;
        } else {
            strDate = "" + intDate;
        }

        int intHour = rightNow.get(Calendar.HOUR_OF_DAY);
        String strHour = null;
        if (intHour < 10) {
            strHour = "0" + intHour;
        } else {
            strHour = "" + intHour;
        }

        int intMinute = rightNow.get(Calendar.MINUTE);
        String strMinute = null;
        if (intMinute < 10) {
            strMinute = "0" + intMinute;
        } else {
            strMinute = "" + intMinute;
        }

        int intSecond = rightNow.get(Calendar.SECOND);
        String strSecond = null;
        if (intSecond < 10) {
            strSecond = "0" + intSecond;
        } else {
            strSecond = "" + intSecond;
        }

        return intYear + "-" + strMonth + "-" + strDate + " " + strHour + ":" + strMinute + ":" + strSecond;

    }

    /**
     * 用来转换日期格式的公用方法
     *
     * @param dateString       需要转换的时间
     * @param sourceDateFormat 原时间格式
     * @param destDateFormat   目的时间格式
     * @return 转换后的时间
     * @throws ParseException 日期格式错误时抛出的异常
     */
    public static String convertDateFormat(String dateString, String sourceDateFormat, String destDateFormat)
            throws ParseException {
        DateFormat dfOne = new SimpleDateFormat(sourceDateFormat);
        DateFormat dfTwo = new SimpleDateFormat(destDateFormat);
        Date date = dfOne.parse(dateString);
        return dfTwo.format(date);
    }

    public static String getthreeMothDate() {
        Calendar time = Calendar.getInstance();
        time.add(Calendar.MONTH, 2);
        int dayNum = time.getActualMaximum(Calendar.DAY_OF_MONTH);
        time.set(Calendar.DATE, dayNum);
        Date date = time.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String format = df.format(date);
        return format;
    }

    public static String formatDateMinute(Date sDate) {
        return formatDate(sDate, YYYY_MM_DD_HH_MM);
    }

    public static Date parseCnMinuteDate(String sDate) {
        return parseDate(sDate, YYYY_MM_DD_HH_MM);
    }

    public static String getDateToString() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static String getNextDateToString(long n) {
        Date date = new Date(System.currentTimeMillis() + 1000 * 3600 * 24 * n);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static Date getNextYearToString(Date date) {
        Calendar time = Calendar.getInstance();
        time.setTime(date);
        time.add(Calendar.YEAR, 1);
        return time.getTime();
    }

    public static String getBeforeDateToString(long n) {
        Date date = new Date(System.currentTimeMillis() - 1000 * 3600 * 24 * n);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static Date getDateNotTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getNowDateWithOutTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static boolean isBeforeEquals(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Date d1 = parseDate(formatDate(date1));
        Date d2 = parseDate(formatDate(date2));
        if (d2 == null || d1 == null) {
            return false;
        }
        return !d1.after(d2);
    }

    public static boolean isAfterEquals(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Date d1 = parseDate(formatDate(date1));
        Date d2 = parseDate(formatDate(date2));
        if (d2 == null || d1 == null) {
            return false;
        }
        return !d1.before(d2);
    }

    public static Boolean isSameEquals(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return formatDate(date1).equals(formatDate(date2));
    }


    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public static int getDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int getHourOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static Date getNextMonth(Date sourceDate) {
        Calendar c = Calendar.getInstance();
        c.setTime(sourceDate);
        c.add(Calendar.MONTH, 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    /**
     * 根据两个日期得出年龄，带小数
     * 注：nowDate大于birthDay的日期时，年龄多加0.5岁（不考虑年份）;owDate小于birthDay的日期时，年龄多减0.5岁（不考虑年份）;
     * 例：过了12岁，就是12.5岁;79未满80，就是79.5岁;
     *
     * @param birthDay
     * @param nowDate
     * @return
     */
    public static double getAge(Date birthDay, Date nowDate) {
        int year = getYear(birthDay);
        int month = getMonth(birthDay);
        int day = getDay(birthDay);
        int year1 = getYear(nowDate);
        int month1 = getMonth(nowDate);
        int day1 = getDay(nowDate);

        double age = year1 - year;
        if (month1 > month || (month1 == month && day1 > day)) {
            age += 0.5;
        }
        if (month1 < month || (month1 == month && day1 < day)) {
            age -= 0.5;
        }
        return age;
    }

    /**
     * 转换long类型的日期
     *
     * @param date    long类型的事件
     * @param pattern 要转换的格式
     * @return 返回的字符串日期格式
     */
    public static String parseDate(long date, String pattern) {

        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }


    /**
     * 根据日期获得星期
     *
     * @param date
     * @return
     */
    public static String getWeekOfDate(Date date) {
        String[] weekDaysName = {"周日", "周一", "周二", "周三", "周四", "周五",
                "周六"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return weekDaysName[intWeek];
    }

    public static Integer getWeekOfDateInt(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return intWeek;
    }


    /**
     * 当前日期分钟相加或相减所得日期（+,-）操作,输入一个日期得到分钟加减后的日期。
     *
     * @param
     * @return
     */
    public static Date DsDay_Minute(Date date, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minutes + minute);
        return calendar.getTime();
    }

    /**
     * 判断两个时间的大小.
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static boolean isCompareTime(Date startTime, Date endTime) {
        if (endTime.getTime() > startTime.getTime()) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 格式时间
     *
     * @param date
     * @param format
     * @return
     */
    public static String getFormatDate(Date date, String format) {
        if (date != null) {
            SimpleDateFormat f = new SimpleDateFormat(format);
            return f.format(date);
        } else {
            return null;
        }
    }

    /**
     * 当前日期小时相加或相减所得日期（+,-）操作,输入一个日期得到天数加减后的日期。
     *
     * @param
     * @return
     */
    public static Date DsDay_Hour(Date date, Integer hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, hours);

        return cal.getTime();
    }

    /**
     * 当前日期分钟相加或相减所得日期（+,-）操作,输入一个日期得到天数加减后的日期。
     *
     * @param
     * @return
     */
    public static Date DsDay_Minute(Date date, Integer minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }


    /**
     * 当前日期小时相加或相减所得日期（+,-）操作,输入一个日期得到天数加减后的日期。
     *
     * @param
     * @return
     */
    public static Date DsDay_Date(Date date, Integer dates) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, dates);

        return cal.getTime();
    }


    public static List<String> getBetweenDays(Date startDay, Date endDay) {
        List<String> middles = new ArrayList<String>();

        Calendar start = Calendar.getInstance();
        start.setTime(startDay);

        Calendar end = Calendar.getInstance();
        end.setTime(endDay);

        Calendar middle = start;
        while (true) {
            if (middle.compareTo(start) >= 0 && middle.compareTo(end) <= 0) {
                middles.add(DateUtil.formatDate(middle.getTime(), DateUtil.YYYY_M_D));
            }

            if (middle.compareTo(end) > 0) {
                break;
            }

            middle.add(Calendar.DATE, 1);
        }
        return middles;
    }

    /**
     * 得到现在时间
     *
     * @return 字符串 yyyyMMdd HHmmss
     */
    public static String getStringToday() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.YYYYMMDDHHMMSS);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 本月第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstdayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * 获得当月1号零时零分零秒
     *
     * @return
     */
    public static Date initDateByMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 本月最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastdayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, value);
        date = cal.getTime();
        return date;
    }


    //list里的数据格式：20180101-20180505
    //判断日期段是否重复
    public static boolean checkOverlap(List<String> list) {
        if (null == list) {
            LoggerUtils.info(log, "比较日期是否重复功能，传入集合为空");
        }

        long count = list.stream().distinct().count();//判断重复，重复就是重叠
        boolean isRepeat = count < list.size();
        if (isRepeat) {
            return true;
        }


        boolean flag = false;//是否重叠标识

        List<String> startList = new ArrayList<>();
        List<String> endList = new ArrayList<>();

        try {
            Collections.sort(list);//排序ASC
            //排重
            for (int i = 0; i < list.size(); i++) {
                String[] itime = list.get(i).split("-");
                if (i != 0) {
                    if (startList.contains(itime[0]) || endList.contains(itime[1])) {
                        flag = true;
                    }
                }
                if (Integer.valueOf(itime[0]) != 0) {
                    startList.add(itime[0]);
                }
                if (Integer.valueOf(itime[1]) != 0) {
                    endList.add(itime[1]);
                }
                //当标识已经认为重叠了则可退出外循环
                if (flag) {
                    LoggerUtils.info(log, "有日期重复");
                    break;
                }
            }

            if (flag) {
                return flag;
            }

            //去掉只有截止日没有开始日的数据
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                String s = (String) it.next();
                if (s.startsWith("0")) {
                    it.remove();
                }
            }


            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    continue;
                }
                if (i > 0) {
                    //跳过第一个时间段不做判断
                    String[] itime = list.get(i).split("-");

                    for (int j = 0; j < list.size(); j++) {
                        //如果当前遍历的i开始时间小于j中某个时间段的结束时间那么则有重叠，反之没有重叠
                        //这里比较时需要排除i本身以及i之后的时间段，因为已经排序了所以只比较自己之前(不包括自己)的时间段
                        if (j == i || j > i) {
                            continue;
                        }

                        String[] jtime = list.get(j).split("-");
                        int compare = Integer.valueOf(itime[0]) - Integer.valueOf(jtime[1]);
                        if (compare <= 0) {
                            flag = true;


                            break;//只要存在一个重叠则可退出内循环
                        }
                    }
                }

                //当标识已经认为重叠了则可退出外循环
                if (flag) {
                    LoggerUtils.info(log, "有日期重复");
                    break;
                }
            }
        } catch (Exception e) {
            LoggerUtils.error(log, "比较日期是否重复功能错误", e);
            throw new BizServiceException(ResponseCodeEnum.BIZ_COMMON_EXCEPTION.getCode(),
                    ResponseCodeEnum.BIZ_COMMON_EXCEPTION.getFormatMsg("比较日期是否重复功能错误！" + e.getMessage()));
        }
        return flag;
    }

    public static Map<String, Object> getGapDate(List<String> list) {
        Map<String, Object> newMap = new LinkedHashMap<>();

        for (int i = 0; i < list.size() - 1; i++) {
            String time1 = list.get(i);
            String time2 = list.get(i + 1);

            String time1s[] = time1.split("-");
            String time2s[] = time2.split("-");

            Date preTime = DateUtil.parseDate(time1s[1], DateUtil.YYYYMMDD);
            Date postTime = DateUtil.parseDate(time2s[0], DateUtil.YYYYMMDD);
            if (postTime == null || preTime == null) {
                throw new BizServiceException(ResponseCodeEnum.BIZ_COMMON_EXCEPTION.getCode(), "getGapDate method exception!");
            }
            int days = DateUtil.getDateDiffByDay(postTime, preTime);


            if (days > 1) {
                String p0 = DateUtil.formatDate(DateUtil.addDays(preTime, 1), DateUtil.YYYYMMDD);
                String p1 = DateUtil.formatDate(DateUtil.addDays(postTime, -1), DateUtil.YYYYMMDD);

                newMap.put(p0 + "-" + p1, "");
            }
        }
        return newMap;
    }

    public static Map<String, Object> getCutDateMap(Map<String, Object> inputDateMap, Map<String, Object> originDateMap) {
        List<String> list = new ArrayList<>();
        for (String key : inputDateMap.keySet()) {
            list.add(key);
        }
        Map<String, Object> gapDateMap = getGapDate(list);
        Map<String, Object> gapResultMap = new LinkedHashMap<>();
        Integer markPosition = cutNewDate(originDateMap, gapDateMap, gapResultMap);

        Iterator iterator = inputDateMap.keySet().iterator();
        String tail = null;
        while (iterator.hasNext()) {
            tail = iterator.next().toString();
        }

        int i = -1;
        if (markPosition == null) {
            markPosition = 0;
        }

        String time[] = tail.split("-");
        String et = time[1];
        boolean putValue = false;
        for (String key : originDateMap.keySet()) {
            ++i;
            if (i >= markPosition) {
                String time2[] = key.split("-");
                String st2 = time2[0];
                String et2 = time2[1];

                if (putValue) {
                    gapResultMap.put(key, originDateMap.get(key));
                }

                if (Integer.valueOf(et) >= Integer.valueOf(st2) && Integer.valueOf(et) < Integer.valueOf(et2)) {
                    Date date = DateUtil.parseDate(et, DateUtil.YYYYMMDD);
                    date = DateUtil.addDays(date, 1);
                    String pr = DateUtil.parseDate(date.getTime(), DateUtil.YYYYMMDD);
                    gapResultMap.put(pr + "-" + et2, originDateMap.get(key));
                    putValue = true;
                }
            }
        }


        //把间隔日期的map整合到新输入日期map
        for (String key : gapResultMap.keySet()) {
            inputDateMap.put(key, gapResultMap.get(key));
        }

        //排序
        List<Map.Entry<String, Object>> lstEntry = new ArrayList<>(inputDateMap.entrySet());
        Collections.sort(lstEntry, ((o1, o2) -> {
            String v1 = o1.getKey().toString().split("-")[0];
            String v2 = o2.getKey().toString().split("-")[0];
            return (v1).compareTo(v2);
        }));

        //返回结果
        Map<String, Object> resultMap = new LinkedHashMap<>();

        lstEntry.forEach(o -> {
            resultMap.put(o.getKey(), o.getValue());
        });
        return resultMap;
    }

    //日期必须是连续的
    public static Map<String, Object> getDateRangeDays(Map<String, Object> resultMap) {
        Map<String, Object> daysMap = new LinkedHashMap<>();
        Integer daysBegin = null;
        Date dateBegin = null;
        for (String key : resultMap.keySet()) {
            String t1 = key.split("-")[0];
            String t2 = key.split("-")[1];

            Date date2 = DateUtil.parseDate(t2, DateUtil.YYYYMMDD);
            if (dateBegin == null) {
                dateBegin = DateUtil.parseDate(t1, DateUtil.YYYYMMDD);
            }
            if (date2 == null || dateBegin == null) {
                throw new BizServiceException(ResponseCodeEnum.BIZ_COMMON_EXCEPTION.getCode(), "getDateRangeDays method exception!");
            }
            int days = DateUtil.getDateDiffByDay(date2, dateBegin) + 1;

            if (daysBegin == null) {
                daysMap.put(1 + "," + days, resultMap.get(key));
                daysBegin = days + 1;
            } else {
                daysMap.put(daysBegin + "," + days, resultMap.get(key));
                daysBegin = days + 1;

            }

        }
        return daysMap;
    }

    //前提：两组数据日期，本身不重叠
    //originMap:原日期段
    //newMap:新输入区间之间的不连续区间
    public static Integer cutNewDate(Map<String, Object> originMap, Map<String, Object> newMap, Map<String, Object> resultMap) {
        int i = -1;
        Integer markPosition = null;

        for (String oKey : originMap.keySet()) {
            i++;
            String[] itime = oKey.split("-");
            String j1_1 = itime[0];
            String j1_2 = itime[1];
            Object rate = originMap.get(oKey);

            int j = 0;
            int jsize = newMap.size();

            for (String key : newMap.keySet()) {
                j++;
                String[] jtime = key.split("-");
                String i1_1 = jtime[0];
                String i1_2 = jtime[1];
                boolean flag1 = false;
                if (Integer.valueOf(i1_1) >= Integer.valueOf(j1_1) && Integer.valueOf(i1_1) <= Integer.valueOf(j1_2)) {
                    if (Integer.valueOf(i1_2) <= Integer.valueOf(j1_2)) {//区间内
                        resultMap.put(i1_1 + "-" + i1_2, rate);
                        flag1 = true;
                    } else {
                        resultMap.put(i1_1 + "-" + j1_2, rate);
                        flag1 = true;
                    }

                    if (flag1 && j == jsize) {
                        markPosition = i;
                    }
                }
            }
            if (markPosition != null) {
                break;
            }
        }
        return markPosition;
    }

    public static String formatDateLong(Long timestamp) {
        try {
            if (timestamp == null || timestamp == 0) {
                return "";
            }
            SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD); //设置格式
            String timeText = format.format(timestamp);
            return timeText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatDateLongString(String timestamp) {
        try {
            if (timestamp == null || StringUtils.isEmpty(timestamp)) {
                return "";
            }
            SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD); //设置格式
            String timeText = format.format(Long.parseLong(timestamp));
            return timeText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTime   指定
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }
//
//    public static void main(String args[]) {
//        long days = DateUtil.getDateDiffByDay("2019-05-21", "2019-08-01");
//        System.out.print(days);
//        Map<String, Object> map = new LinkedHashMap<>();
//        map.put("20190601-20200109", "10"); //长时间
//
//        map.put("20190601-20190801", "2");
//        map.put("20190910-20191101", "2");
//        map.put("20191210-20200105", "2");
//
//        map.put("20190601-20190701", 9);
//        map.put("20190910-20191101", 6);
//        map.put("20191210-20200105", 7);
//
//
//        Map<String, Object> originMap = new LinkedHashMap<>();
//        originMap.put("20190101-20190731", 2);
//        originMap.put("20190801-20191030", 3);
//        originMap.put("20191031-20191210", 4);
//        originMap.put("20191211-20200110", 5);
//
//        Map<String, Object> result = getCutDateMap(map, originMap);
//
//        System.out.println("结果 ==================");
//        for (String key : result.keySet()) {
//            System.out.println(key + " " + result.get(key));
//        }
//
//        Map<String, String> dateMap = new LinkedHashMap<>();
//        dateMap.put("20190101-20190331", "1");
//        dateMap.put("20190401-20190630", "1");
//        dateMap.put("20190701-20190930", "1");
//        dateMap.put("20191001-20191231", "1");
////        getDays(dateMap);
//    }
}