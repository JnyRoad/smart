package com.tce.smart.app.ao.wechat;

import java.util.List;

import com.tce.smart.app.ao.fore.MemberAo;
import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 被访人信息查询Ao
 *
 * @author mingkai.wu
 * @date 2019-05-13 08:28:54
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddVisitMemberAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8687754044704419673L;

	/**
	 * 被访人员工号
	 */
	private String visitId;

	/**
	 * 随行人员列表
	 */
	private List<MemberAo> member;
}
