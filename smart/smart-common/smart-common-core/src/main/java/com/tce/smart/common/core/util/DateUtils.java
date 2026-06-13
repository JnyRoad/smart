package com.tce.smart.common.core.util;

import cn.hutool.core.date.DateUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * @ClassName: DateUtils
 * @Package com.tce.yunshi.common.util
 * @Description: 日期时间工具类
 * @Author wuxinjian
 * @Date 2018/11/1 11:05
 * @Version V1.0
 */
public class DateUtils extends DateUtil {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_MINUTE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String POSTFEX = " 00:00:00";

    public static final String SECOND_POSTFEX = ":00";

    /**
     * 当前时间LocalDateTime对象
     *
     * @return LocalDateTime
     * @Title localDateTime
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime localDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 当前时间LocalDateTime对象,格式化去掉毫秒
     *
     * @return LocalDateTime
     * @Title localDateTime
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime localDateTime(boolean format) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (!format) {
            return localDateTime;
        }
        String time = convert(localDateTime);
        return parseLocalDateTime(time);
    }

    /**
     * 获取
     *
     * @return LocalDateTime
     * @Title localDateTime
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime localDateTime(LocalDateTime localDateTime, int time) {
        return middleNight(localDateTime).plusHours(time);
    }

    /**
     * 当前时间戳 - 秒
     *
     * @return long
     * @Title now
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static long toEpochSecond() {
        return toEpochSecond(localDateTime());
    }

    /**
     * 当前时间戳 - 毫秒
     *
     * @return long
     * @Title now
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static long toEpochMilli() {
        return toEpochMilli(localDateTime());
    }

    /**
     * 1个月前的时间
     *
     * @return long
     * @Title now
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime lastMonthOfLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.minus(1, ChronoUnit.MONTHS);
    }

    /**
     * n天前的时间
     *
     * @return long
     * @Title now
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime lastDay(int n) {
        LocalDateTime now = LocalDateTime.now();
        return now.minus(n, ChronoUnit.DAYS);
    }

    /**
     * n 年前 的时间
     *
     * @return long
     * @Title now
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime lastYear(int n) {
        LocalDateTime now = LocalDateTime.now();
        return now.minus(n, ChronoUnit.YEARS);
    }

    /**
     * N天前的时间
     *
     * @param startLocalDateTime
     * @param n
     * @return
     */
    public static LocalDateTime lastDayTime(LocalDateTime startLocalDateTime, Integer n) {
        return startLocalDateTime.minus(n, ChronoUnit.DAYS);
    }

    /**
     * 0点
     *
     * @return LocalDateTime
     * @Title middleNight
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime middleNight() {
        return middleNight(localDateTime());
    }

    /**
     * 0点
     *
     * @return LocalDateTime
     * @Title middleNight
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime middleNight(LocalDateTime localDateTime) {
        return parseLocalDateTime(DEFAULT_DATE_TIME_FORMAT, convert(DATE_FORMAT, localDateTime) + POSTFEX);
    }

    /**
     * 整数分钟,默认格式yyyy-MM-dd HH:mm
     *
     * @param localDateTime
     * @return LocalDateTime
     * @Title currentMinuteTime
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime currentMinuteTime(LocalDateTime localDateTime) {
        return parseLocalDateTime(DEFAULT_DATE_TIME_FORMAT, convert(DEFAULT_MINUTE_DATE_TIME_FORMAT, localDateTime) + SECOND_POSTFEX);
    }

    /**
     * 整数分钟,默认格式yyyy-MM-dd HH:mm
     *
     * @param target
     * @return boolean
     * @Title middleNight
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static boolean isMiddleNight(LocalDateTime target) {
        LocalDateTime targetTime = parseLocalDateTime(DEFAULT_DATE_TIME_FORMAT, convert(DEFAULT_MINUTE_DATE_TIME_FORMAT, target) + SECOND_POSTFEX);
        return targetTime.isEqual(middleNight());
    }

    /**
     * LocalDateTime转String字符串,默认格式yyyy-MM-dd HH:mm:ss
     *
     * @param localDateTime
     * @return String
     * @Title convert
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static String convert(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return StringUtils.EMPTY;
        }
        return convert(DEFAULT_DATE_TIME_FORMAT, localDateTime);
    }

    /**
     * LocalDateTime转String字符串,默认格式yyyy-MM-dd
     *
     * @param localDateTime
     * @return String
     * @Title convert
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static String format(LocalDateTime localDateTime) {
        return convert(DATE_FORMAT, localDateTime);
    }

    /**
     * LocalDateTime转String字符串
     *
     * @param format
     * @param localDateTime
     * @return String
     * @Title convert
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static String convert(String format, LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }

    /**
     * String字符串转LocalDateTime,默认格式yyyy-MM-dd HH:mm:ss
     *
     * @param time
     * @return LocalDateTime
     * @Title parse
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime parseLocalDateTime(String time) {
        return parseLocalDateTime(DEFAULT_DATE_TIME_FORMAT, time);
    }

    /**
     * String字符串转LocalDateTime
     *
     * @param time
     * @return LocalDateTime
     * @Title convert
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime parseLocalDateTime(String format, String time) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(time, df);
    }

    /**
     * 秒 long 型时间戳转LocalDateTime
     *
     * @param timestamp
     * @return LocalDateTime
     * @Title ofEpochMilli
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime ofEpochSecond(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * 毫秒 long 型时间戳转LocalDateTime
     *
     * @param timestamp
     * @return LocalDateTime
     * @Title ofEpochMilli
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static LocalDateTime ofEpochMilli(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * LocalDateTime转long型时间戳
     *
     * @param localDateTime
     * @return long
     * @Title convert
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static long toEpochMilli(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * LocalDateTime转long型时间戳
     *
     * @param localDateTime
     * @return long
     * @Title convert
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static long toEpochSecond(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * LocalDateTime 转 星期几
     *
     * @param localDateTime
     * @return DayOfWeek
     * @Title convert
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static DayOfWeek week(LocalDateTime localDateTime) {
        return localDateTime.getDayOfWeek();
    }

    /**
     * 获取当前日期是 星期几
     *
     * @param
     * @return DayOfWeek
     * @Title convert
     * @author wuxinjian
     * @date 2018/11/1 11:16
     */
    public static DayOfWeek week() {
        return week(localDateTime());
    }

    public static LocalTime time() {
        LocalDateTime localDateTime = localDateTime();
        return localDateTime.toLocalTime();
    }

    public static LocalTime time(String time) {
        return LocalTime.parse(time);
    }

	public static String format(LocalDate localDate){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		return localDate.format(formatter);
	}
}
