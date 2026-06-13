package com.tce.smart.app.api.vo;


import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调休类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchAdjustVO extends Model<SearchAdjustVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 调休id
	 */
	private String termId;
	/**
	 * 出勤日期标题
	 */
	private String workDate;
	/**
	 * 出勤日期
	 */
	private Date term;
	/**
	 * 出勤日期剩余的调休天数
	 */
	private Double termCount;


}
