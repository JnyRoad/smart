package com.tce.smart.platform.core.ao;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/***
 * description: 园区组织关系保存Ao <br>
 * date: 2019/11/27 15:21 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkOrgSetSaveAO extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 4489551795960707419L;

	/**
	 * 园区ID
	 */
	@NotNull(message = "园区ID不能为空")
	private Integer parkId;

	/**
	 * 物流中心ID
	 */
	@NotNull(message = "物流中心ID不能为空")
	private String logisticId;

	/**
	 * 关联BU
	 */
	@NotNull(message = "关联BU不能为空")
	@NotEmpty(message = "关联BU不能为空")
	private List<String> workCompList;

	/**
	 * 入园申请的职层
	 */
	private List<String> jcheList;

}
