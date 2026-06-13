package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 补卡类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchPatchCardReasonRespDTO implements Serializable {
	private static final long serialVersionUID = -389263572793643148L;

	/**
	 * 类型编号
	 */
	private String reasonCode;
	/**
	 * 类型名称
	 */
	private String reasonName;


}
