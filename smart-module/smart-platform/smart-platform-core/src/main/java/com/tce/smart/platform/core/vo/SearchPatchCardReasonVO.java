package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 补卡类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchPatchCardReasonVO extends Model<SearchPatchCardReasonVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 类型编号
	 */
	private String reasonCode;
	/**
	 * 类型名称
	 */
	private String reasonName;


}
