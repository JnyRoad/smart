package com.tce.smart.tool.util;

import cn.hutool.core.util.ReUtil;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: RegexUtils2
 * @Author jinbo
 * @Date 2019/4/17
 */
public class RegexUtils extends ReUtil {
    /**
     * status正则
     */
    private static final String VISITOR_STATUS = "^[0-4]$";

    /**
     * 年月份正则
     */
    private static final String YEAR_MONTH = "^\\d{4}-((0([1-9]))|(1(0|1|2)))$";
    /**
     * 年月日份正则
     */
    private static final String YEAR_MONTH_DAY = "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$";
    /**
     * eventType正则
     */
    private static final String EVENT_TYPE = "^[1-2]$";


    /**
     * 车牌号正则
     */
	//private static final String RE_VEHICLE_CODE = "^([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})$";
	private static final String RE_VEHICLE_CODE = "^([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]?[A-Z]{1}(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]?[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})$";

	/**
     * 正则表达式：验证手机号
     */
    public static final String RE_MOBILE = "^1\\d{10}$";
/*    public static final String RE_MOBILE = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";

/**
     * 正则表达式：验证手机号2  17715856248\1552632455格式
     */
	public static final String RE_MOBILE_B = "^1\\d{10}.*";
	/**
     * 邮箱正则
     */
	private static final String RE_EMAIL = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";

	/**
	 * 名称正则(汉字、英文、数字及下划线)
	 */
	private static final String RE_NAME = "^[\\w|\\u4e00-\\u9fa5]{1,30}$";

	/**
	 * 时长正则
	 */
	private static final String DURATION = "^[0-9]{1,7}$";
	/**
	 * 名称正则(汉字、英文、数字及下划线)
	 */
	private static final String RE_NAME_WITH_SPACE = "^[\\s\\w|\\u4e00-\\u9fa5]{1,30}$";

    /**
     * 编码\用户名等正则(英文、数字及下划线)
     */
	private static final String RE_CODE = "^[\\w]{1,30}$";

    /**
     * 中文正则
     */
	private static final String RE_CHINESE = "^[\\u4e00-\\u9fa5]+$";

    /**
     * 时间格式正则2018-06-09 23:22:22
     */
	private static final String DATE_CODE = "^(\\d{4})(\\-)(\\d{2})(\\-)(\\d{2})(\\s+)(\\d{2})(\\:)(\\d{2})(\\:)(\\d{2})$";

	/**
	 * 员工年龄 19-70
	 */
	private static final String AGE_CODE ="^(1[89]|[2-8]\\d|70)$";

	/**
	 * 经度，经度整数部分为0-180,小数部分为0到6位!
	 */
	private static final String  LONGITUDE_CODE ="((\\d|[1-9]\\d|1[0-7]\\d)°(\\d|[0-5]\\d)′(\\d|[0-5]\\d)(\\.\\d{1,6})?\\″)|(180°0′0\\″)";


	/**
	 * 纬度整数部分为0-90,小数部分为0到6位!
	 */
	private static final String  LATITUDE_CODE = "((\\d|[1-8]\\d)°(\\d|[0-5]\\d)′(\\d|[0-5]\\d)(\\.\\d{1,6})?\\″)|(90[°]0[′]0\\″)";

	/**
	 * 正整数
	 */
	private static final String  NUMBER_CODE="^[0-9]*$";

	/**
	 * 固定电话，座机
	 */
	private static final String RE_TELEPHONE="^0\\d{2,3}-\\d{7,8}$";

	/**
	 * 身份证号正则表达式
	 */
	private static final String CENTO_CODE="(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";

	/**
	 * 数字字母正则表达式
	 */
	private static final String ALPHAN_NUMBER="[A-Za-z0-9]+";

	/**
	 * 匹配年月份的
	 * @param code
	 * @return
	 */
	public static boolean matchYearMonth(String code){
		return isMatch(YEAR_MONTH, code);
	}
	/**
	 * 匹配年月日份的
	 * @param code
	 * @return
	 */
	public static boolean matchYearMonthDay(String code){
		return isMatch(YEAR_MONTH_DAY, code);
	}
    /**
     * 匹配访客的状态
     * 只0-5
     * @param code
     * @return
     */
    public static boolean matchStatus(String code){
        return isMatch(VISITOR_STATUS, code);
    }
    /**
     * 匹配抓拍人员的事件类型
     * 只1-2
     * @param code
     * @return
     */
    public static boolean matchEventType(String code){
	return isMatch(EVENT_TYPE, code);
    }

    /**
	 * 匹配姓名
	 * 只允许汉字、字母与数字的组合
	 * @param name
	 * @return
	 */
	public static boolean matchName(String name){
		return isMatch(RE_NAME, name);
	}
	/**
	 * 匹配姓名
	 * 只允许汉字、字母与数字,空格的组合
	 * @param name
	 * @return
	 */
	public static boolean matchNameWithSpace(String name){
		return isMatch(RE_NAME_WITH_SPACE, name);
	}
    /**
     * 匹配编码\用户名等
     * 只允许字母与数字的组合
     * @param code
     * @return
     */
    public static boolean matchCode(String code){
        return isMatch(RE_CODE, code);
    }
	/**
	 * 只允许汉字
	 * @param chinese
	 * @return
	 */
	public static boolean matchChinese(String chinese){
		return isMatch(RE_CHINESE, chinese);
	}
	/**
	 * 时间正则
	 * @param time
	 * @return
	 */
	public static boolean matchDate(String time){
		return isMatch(DATE_CODE, time);
	}

	/**
	 * 手机号正则
	 * @param phone
	 * @return
	 */
	public static boolean matchPhone(String phone){
		return isMatch(RE_MOBILE, phone);
	}

    /**
     * 判断邮箱
     * @param email
     * @return
     */
    public static boolean matchEmail(String email){
        return isMatch(RE_EMAIL, email);
    }
	/**
	 * 判断车牌号(包括新能源汽车)
	 * @param vehicleCode
	 * @return
	 */
	public static boolean matchVehicle(String vehicleCode){
		return isMatch(RE_VEHICLE_CODE, vehicleCode);
	}


	/**
	 * 匹配年龄
	 * 18-80
	 * @param code
	 * @return
	 */
	public static boolean matchAge(String code){
		return isMatch(AGE_CODE, code);
	}

	/**
	 * 匹配时长
	 * @param code
	 * @return
	 */
	public static boolean matchDuration(String code){
		return isMatch(DURATION, code);
	}


	/**
	 * 经度正则表表达式
	 * 经度整数部分为0-180,小数部分为0到6位!
	 * @param code
	 * @return
	 */
	public static boolean matchLongitude(String longitude){
		return isMatch(LONGITUDE_CODE, longitude);
	}


	/**
	 * 纬度正则表表达式
	 * 纬度整数部分为0-90,小数部分为0到6位
	 * @param code
	 * @return
	 */
	public static boolean matchLatitude(String latitude){
		return isMatch(LATITUDE_CODE, latitude);
	}

	/**
	 * 正整数
	 * @param number
	 * @return
	 */
	public static boolean matchNumber(String number){
		return isMatch(NUMBER_CODE, number);
	}


	public static boolean matchTelephone(String telPhone){
		return isMatch(RE_TELEPHONE, telPhone);
	}

	/**
	 * 身份证号
	 * @param cento
	 * @return
	 */
	public static boolean matchCento(String cento){
		return isMatch(CENTO_CODE, cento);
	}

	/**
	 * 判断是否工号
	 * @param badge
	 * @return
	 */
	public static boolean matchBadge(String badge){
		return isMatch(ALPHAN_NUMBER, badge);
	}


}
