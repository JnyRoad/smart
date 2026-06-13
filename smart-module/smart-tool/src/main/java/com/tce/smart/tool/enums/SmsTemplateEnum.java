package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 短信模板枚举
 *
 * @author mingkai.wu
 * @date 2019-05-15 20:12:18
 */
public enum SmsTemplateEnum {
	//访客预约通知
	VISIT_1001("1001", "预约成功通知"),
	VISIT_1002("1002", "预约失败通知"),
	VISIT_1003("1003", "来访到时通知"),
	VISIT_1004("1004", "访客到访通知"),
	VISIT_1005("1005", "预约申请通知"),
	VISIT_1006("1006", "预约成功通知被访人"),
	VISIT_1007("1007", "访客离开通知"),
	VISIT_1008("1008", "主管审批访客通知"),
	VISIT_1009("1009", "访客超时未离通知"),
	VISIT_10010("10010", "预约失败通知"),
	VISIT_10011("10011", "预约失败通知"),
	VISIT_10012("10012", "访客审批代理人通知"),

	//短息发送失败
	SMS_12001("12001","短信失败反馈通知"),

	//招聘通知
	RECRUIT_2001("2001", "面试通知"),
	RECRUIT_2002("2002", "复试通知"),
	RECRUIT_2003("2003", "录取通知"),

	//邮件模板
	EMAIN_2201("2201","裕同集团-面试通知"),
	EMAIN_5001("5001","裕同集团-入职准备工作"),
	EMAIN_6001("6001","裕同集团-入职通知"),

	//离职通知
	DIMISSION_3001("3001", "离职完成通知"),

	//物流车预约通知
	GUARD_11001("11001", "物流车预约通知"),

	//短信验证码
	SMSCODE_4001("4001", "手机验证通知"),

	//App消息推送
	APP_PUSH_1301("1301","外向内预约审批通知"),
	//招聘类
	APP_PUSH_2301("2303","简历投递通知HR"),
	APP_PUSH_2302("2302","员工入职"),
	//离职类
	APP_PUSH_2303("3301","离职审批通过通知"),
	APP_PUSH_2304("3302","离职工作交接通知"),
	APP_PUSH_2305("3303","离职工作交接完成通知"),
	APP_PUSH_2306("3304","离职审批拒接通知"),
	//住宿类通知
	APP_PUSH_6301("6301","外宿申请通过"),
	APP_PUSH_6302("6302","外宿申请退回"),
	APP_PUSH_6303("6303","外宿补贴撤销申请通过"),
	APP_PUSH_6304("6304","外宿补贴撤销申请退回"),
	APP_PUSH_6305("6305","外餐申请通过"),
	APP_PUSH_6306("6306","外餐申请退回"),
	//其他通知
	APP_PUSH_7301("7301","请假申请通过"),
	APP_PUSH_7302("7302","请假申请退回"),
	APP_PUSH_8301("8301","调休申请通过"),
	APP_PUSH_8302("8302","调休申请退回"),
	APP_PUSH_9301("9301","加班申请通过"),
	APP_PUSH_9302("9302","加班申请退回"),
	APP_PUSH_9303("9303","出差申请通过"),
	APP_PUSH_9304("9304","出差申请退回"),
	APP_PUSH_10301("10301","补卡申请通过"),
	APP_PUSH_10302("10302","考勤异常通知"),
	APP_PUSH_10303("10303","补卡申请退回"),

	SMS_SIGN_10401("10401","工资签收提醒通知"),
	SMS_SIGN_10402("10402","考勤汇总确认通知"),

	SMS_BADGE_10501("10501","厂牌补领领取地址通知"),
	SMS_BADGE_10502("10502","厂牌补领申请拒绝通知"),
	APP_PUSH_10503("10503","厂牌补领申请通过"),
	APP_PUSH_10504("10504","厂牌补领申请退回"),

	SMS_RELEASE_10601("10601","物品放行申请通知"),
	APP_RELEASE_10602("10602","物品放行申请通过"),
	APP_RELEASE_10603("10603","物品放行申请退回"),
	APP_RELEASE_10604("10604","物品放行申请通知"),
	SMS_RELEASE_10605("10605","物品放行审批通过"),
	SMS_RELEASE_10606("10606","物品放行审批回退"),

	APP_APPEAL_10701("10701","申诉回复完成通知"),
	APP_APPEAL_10702("10702","申诉申请通知"),

	APP_REPAIR_10801("10801","园区报修申请通知"),
	APP_REPAIR_10802("10802","园区报修审批完成通知"),
	APP_REPAIR_10803("10803","园区报修审批退回通知"),
	SMS_REPAIR_10804("10804","园区报修审批通知"),

	//入厂申请微信推送
	WECHAT_ADMITTANCE_10901("10901","预约成功通知（访客）"),
	WECHAT_ADMITTANCE_10902("10902","预约失败通知（访客）"),
	WECHAT_ADMITTANCE_10903("10903","来访到时通知"),
	WECHAT_ADMITTANCE_10904("10904","访客到访通知"),
	WECHAT_ADMITTANCE_10905("10905","访客离开通知"),
	WECHAT_ADMITTANCE_10906("10906","访客超时未离通知"),

	//保密区申请微信消息推送
	WECHAT_SECURITY_11101("11101","保密区门禁权限申请结果通知"),

	//退宿申请微信消息推送
	WECHAT_DORMITORY_QUIT_11201("11201","退宿申请--通知室友"),
	WECHAT_DORMITORY_QUIT_11202("11202","退宿申请--通知宿管"),
	WECHAT_DORMITORY_QUIT_11203("11203","退宿申请- 通知申请人成功"),
	WECHAT_DORMITORY_QUIT_11204("11204","退宿申请- 通知申请人失败"),

	//园区报修
	WECHAT_REPAIR_11301("11301","园区报修-通知处理人"),
	WECHAT_REPAIR_11302("11302","园区报修-通知申请人"),

	//人脸下发
	WECHAT_FACE_11401("11401","员工人脸下发异常通知"),

	// 物品放行申请（生活区）
	WECHAT_RELEASE_11501("11501","物品放行申请（生活区）-通知室友"),
	WECHAT_RELEASE_11502("11502","物品放行申请（生活区）-通知宿管"),
	WECHAT_RELEASE_11503("11503","物品放行申请（生活区）-通知申请人成功"),
	WECHAT_RELEASE_11504("11504","物品放行申请（生活区）-通知申请人失败");

	private final String code;

	private final String desc;

	SmsTemplateEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SmsTemplateEnum desc(String code) {
		if (Objects.nonNull(code)) {
			for (SmsTemplateEnum enmuType : SmsTemplateEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static String code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (SmsTemplateEnum typeEnmu : SmsTemplateEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
