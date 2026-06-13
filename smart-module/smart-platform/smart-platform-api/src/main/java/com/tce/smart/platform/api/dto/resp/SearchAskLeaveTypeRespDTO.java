package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 请假类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchAskLeaveTypeRespDTO implements Serializable {
	private static final long serialVersionUID = -8207465261248492403L;

	/**
	 * 类型编号
	 */
	private String vacateCode;
	/**
	 * 类型名称
	 */
	private String vacateName;

	/**
	 * 类型说明
	 */
	private String vacateRemark;


}
