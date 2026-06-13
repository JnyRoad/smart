package com.tce.smart.platform.utils;

import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.vo.EmpHrVO;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Objects;

/**
 * @author sunfujian
 * @since 2021/9/8 9:31
 */
public class StaffUtil {

	/**
	 * 根据身份证号计算年龄
	 *
	 * @param IdNO
	 * @return
	 */
	public static Integer idNoToAge(String IdNO) {
		String birthTimeString = IdNO.substring(6, 10) + "-" + IdNO.substring(10, 12) + "-" + IdNO.substring(12, 14);
		// 先截取到字符串中的年、月、日
		String[] strs = birthTimeString.trim().split("-");
		int selectYear = Integer.parseInt(strs[0]);
		int selectMonth = Integer.parseInt(strs[1]);
		int selectDay = Integer.parseInt(strs[2]);
		// 得到当前时间的年、月、日
		Calendar cal = Calendar.getInstance();
		int yearNow = cal.get(Calendar.YEAR);
		int monthNow = cal.get(Calendar.MONTH) + 1;
		int dayNow = cal.get(Calendar.DATE);
		// 用当前年月日减去生日年月日
		int yearMinus = yearNow - selectYear;
		int monthMinus = monthNow - selectMonth;
		int dayMinus = dayNow - selectDay;
		int age = yearMinus;
		if (yearMinus < 0) {// 选了未来的年份
			age = 0;
		} else if (yearMinus == 0) {// 同年的，要么为1，要么为0
			if (monthMinus < 0) {// 选了未来的月份
				age = 0;
			} else if (monthMinus == 0) {// 同月份的
				if (dayMinus < 0) {// 选了未来的日期
					age = 0;
				} else {
					age = 1;
				}
			} else {
				age = 1;
			}
		} else {
			if (monthMinus < 0) {// 当前月>生日月
			} else if (monthMinus == 0) {// 同月份的，再根据日期计算年龄
				if (dayMinus < 0) {
				} else {
					age = age + 1;
				}
			} else {
				age = age + 1;
			}
		}
		return age;
	}

	/**
	 * 生成员工工号
	 *
	 * @param lastStaff 工号最大的员工
	 * @param comp      BU的缩写
	 * @return 新的员工号
	 */
	public static String getNewBadge(SmtStaff lastStaff, String comp) {
		String newBadge = "";
		DecimalFormat countFormat = new DecimalFormat("000000");
		if (lastStaff != null) {
			Integer maxCode = Integer.parseInt(lastStaff.getBadge());
			newBadge = comp + countFormat.format(maxCode + 1);
		} else {
			newBadge = comp + countFormat.format(1);
		}
		return newBadge;
	}

	/**
	 * 根据汉字获取拼音
	 * @param inputString
	 * @return
	 */
	public static String getPinYin(String inputString) {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		char[] input = inputString.trim().toCharArray();
		StringBuilder output = new StringBuilder();
		try {
			for (int i = 0; i < input.length; i++) {
				if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {  //判断字符是否是中文
					//toHanyuPinyinStringArray 如果传入的不是汉字，就不能转换成拼音，那么直接返回null
					//由于中文有很多是多音字，所以这些字会有多个String，在这里我们默认的选择第一个作为pinyin
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
					output.append(temp[0]);
				} else {
					output.append(input[i]);
				}
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return output.toString();
	}

	public static void buildStaff(EmpHrVO empHr, SmtStaff smtStaff) {
		smtStaff.setName(empHr.getName());
		smtStaff.setBadge(empHr.getBadge());
		smtStaff.setJobId(String.valueOf(empHr.getJobid()));
		smtStaff.setJobName(empHr.getJobname());
		smtStaff.setCompId(String.valueOf(empHr.getCompID()));
		smtStaff.setCompName(empHr.getCompname());
		smtStaff.setDepId(String.valueOf(empHr.getDepid()));
		smtStaff.setDepName(empHr.getDepname());
		smtStaff.setJcheId(String.valueOf(empHr.getJchenID()));
		smtStaff.setJcheName(empHr.getJchenName());
		smtStaff.setWelfareLevel(empHr.getFlcj());
		smtStaff.setCertno(empHr.getCertno());
		smtStaff.setSex(empHr.getGender());
		smtStaff.setPqcompany(empHr.getPqcompany());
		smtStaff.setAge(empHr.getAge());
		if (Objects.nonNull(empHr.getLeaDate())) {
			smtStaff.setLeaDate(empHr.getLeaDate());
		}
		if (empHr.getBirthDay() != null) {
			smtStaff.setBirth(DateUtils.format(empHr.getBirthDay(), DateUtils.DATE_FORMAT));
		}
		if (empHr.getPhone() != null) {
			String phone = empHr.getPhone().trim();
			if (phone.length() > 11) {
				//只取前11个字符
				String realPhone = "";
				for (int i = 0; i < phone.length() && realPhone.length() < 11; i++) {
					if (phone.charAt(i) >= 48 && phone.charAt(i) <= 57) {
						realPhone += phone.charAt(i);
					}
				}
				smtStaff.setPhone(realPhone);
			} else {
				smtStaff.setPhone(phone);
			}
		}
		smtStaff.setEmail(empHr.getEmail());
		smtStaff.setStatus(empHr.getStatus());
		smtStaff.setCreateTime(empHr.getJoindate());
		smtStaff.setEId(empHr.getEid());
		smtStaff.setReportTo(empHr.getReportTo());
		smtStaff.setEmpType(empHr.getEmpType());
		smtStaff.setPzid(empHr.getPzid());
		smtStaff.setDepAbbr(empHr.getDepAbbr());
		smtStaff.setNation(empHr.getNation());
		smtStaff.setResidentaddress(empHr.getResidentaddress());
		smtStaff.setLeaType(empHr.getLeaType());
	}
}
