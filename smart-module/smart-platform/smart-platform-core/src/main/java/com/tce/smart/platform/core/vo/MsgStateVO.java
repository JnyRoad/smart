package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信发送状态
 *
 * @author fushiping
 * @date 2019/10/9 16:49
 **/
@Data
public class MsgStateVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -7127713342027138545L;

	/**
	 * 状态id
	 */
	private Integer stateId;

	/**
	 * 状态
	 */
	private String state;
}
