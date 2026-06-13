package com.tce.smart.common.core.util;

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
     * eventType正则
     */
    private static final String EVENT_TYPE = "^[1-2]$";


    /**
     * 车牌号正则
     */
	private static final String RE_VEHICLE_CODE = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4,5}[A-Z0-9挂学警港澳]{1}$";

	 /**
     * 正则表达式：验证手机号
     */
    public static final String RE_MOBILE = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
	/**
     * 邮箱正则
     */
	private static final String RE_EMAIL = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";

	/**
	 * 名称正则(汉字、英文、数字及下划线)
	 */
	private static final String RE_NAME = "^[\\w|\\u4e00-\\u9fa5]{1,30}$";
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

}
