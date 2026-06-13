package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.List;

/**
 * @Auther: 付世平
 * @Date: 2020-08-09 21:13
 */
@Data
public class DormitorySituationRespDTO extends BaseVO {


	/**
	 * 是否存在外宿补贴 0 无  1 存在
	 */
	private Integer isOutDormitory;

	/**
	 * 已存在宿舍房间号
	 */
	private List<String> rooms;

	/**
	 * 是否允许申请宿舍 true 允许  false 不允许
	 */
	private Boolean isPass;

	/**
	 * 错误楼栋
	 */
	private String errorDor;
}
