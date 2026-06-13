package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author fushiping
 * @date 2019/10/10 10:07
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class MsgInfoVO extends BaseVO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2960614429015126898L;

	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;


	/**
	 * 模板名称
	 */
	private String tempName;

	/**
	 * 消息接收对象
	 */
	private String msgObject;

	/**
	 * 消息内容
	 */
	private String msgContent;

	/**
	 * 消息备注（失败原因）
	 */
	private String msgDesc;

	/**
	 * 消息状态：0-初始化，1-发送成功，2-发送失败'
	 */
	private Integer msgState;

	/**
	 * 消息发送状态
	 */
	private String msgStateName;

	/**
	 * 更新时间
	 */
	private LocalDateTime createTime;


}
