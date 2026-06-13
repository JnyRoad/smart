package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: Liu.jihong
 * @Date: 2020/9/27 16:01
 */
@Data
public class SmtMsgRecordRespDTO extends BaseVO {
	/**
	 * 主键ID
	 */
	private Integer id;

	/**
	 * 模板Id
	 */
	private Integer tempId;

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
	 * 消息状态：0-初始化，1-发送成功，2-发送失败'
	 */
	private Integer msgState;

	/**
	 * 消息状态中文
	 */
	private String msgStateName;

	/**
	 * 是否已读：0-未读，1-已读
	 */
	private Integer readState;

	/**
	 * 消息描述
	 */
	private String msgDesc;

	/**
	 * 备用字段1
	 */
	private String remark1;

	/**
	 * 备用字段3
	 */
	private String remark2;

	/**
	 * 备用字段2
	 */
	private String remark3;

	/**
	 * 是否已删除：0-删除，1-正常
	 */
	private Integer deleteState;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;
}
