package com.tce.smart.tool.constant;

import lombok.experimental.UtilityClass;

/**
 * @description: 符号常量类
 * @date: 2020-07-10 19:01:45
 * @author: fushiping
 * @version: 1.0
 */
@UtilityClass
public class SymbolConstants {

    /**
     * 句号-半角-英文
     */
    public final static String FULL_POINT = ".";

    /**
     * 逗号-半角-英文
     */
    public final static String COMMA = ",";

    /**
     * 冒号-半角-英文
     */
    public final static String COLON = ":";

	/**
	 * 斜杠-英文
	 */
	public final static String BRANCH = "/";

    /**
     * 冒号-半角-中文
     */
    public final static String COLON_CN = "：";

    /**
     * 空格-半角-英文
     */
    public final static String BLANK = " ";

	/**
	 * 空字符串
	 */
	public final static String NULL_STRING = "";

    /**
     * 编号替换参数-半角-英文
     */
    public final static String BRACE = "{}";

    /**
     * 下划线-半角-英文
     */
    public final static String UNDER_LINE = "_";

    /**
     * 减号-半角-英文
     */
    public final static String MINUS = "-";

    /**
     * word-方框-半角-英文
     */
    public final static String BOX_EMPTY = "□";

    /**
     * word-方框勾-英文
     */
    public final static String BOX_RIGHT = "☑";

    /**
     * 艾特-半角-英文
     */
    public final static String AT = "@";

    /**
     * 时间格式-yyyyMMdd
     */
    public final static String DATE_FORMAT_YYYY_MM_DD = "yyyyMMdd";

	/**
	 * 时间格式-yyyyMMdd
	 */
	public final static String DATE_FORMAT_YYYY_MM_DD_B = "yyyy-MM-dd";

	/**
	 * 时间格式-yyyyMMddhhmm
	 */
	public final static String DATE_FORMAT_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    /**
     * 时间格式-yyyyMMdd
     */
    public final static String DATE_TIME_LATEST = "23:59:59";

    /**
     * 时间格式-yyyyMMdd
     */
    public final static String DATE_FORMAT_YYYY_MM = "yyyy-MM";

	/**
	 * 时间格式-yyyyMMdd
	 */
	public final static String DATE_TIME_START = "00:00:00";

    /**
     * 时间格式-yyyy年 MM月 dd日 HH时 mm分
     */
    public final static String DATE_TIME_CHINA_BLANK = "yyyy年 MM月 dd日 HH时 mm分";

	/**
	 * 时间格式-yyyy年 MM月
	 */
	public final static String DATE_TIME_OF_MONTH = "yyyy年 MM月";

    /**
     * 时间格式-yyyy年  MM月  dd日
     */
    public final static String DATE_TIME_CHINA_TWO_BLANK = "yyyy年  MM月  dd日";

    /**
     * 时间格式-yyyy年MM月dd日（E）HH:mm
     */
    public final static String DATE_TIME_CHINA_WEEK = "yyyy年MM月dd日（E）HH:mm";

    /**
     * 数字-0
     */
    public final static Integer ZERO_INTEGER = 0;

	/**
	 * 数字-1
	 */
	public final static Integer ONE_INTEGER = 1;

	/**
	 * 数字-1
	 */
	public final static String ONE_STRING = "1";

    /**
     * 字符-0
     */
    public final static Character ZERO_STRING = '0';

    /**
     * 案件列表上次搜索时间
     */
    public final static String LAST_SEARCH_TIME = "lastSearchTime";

    /**
     * 文件后缀名-zip
     */
    public final static String FILE_SUFFIX_ZIP = "zip";

    /**
     * 文件后缀名-docx
     */
    public final static String FILE_SUFFIX_DOCX = "docx";


	public final static String EHR_STR = "EHR";

	public final static String DHR_STR = "DHR";
}
