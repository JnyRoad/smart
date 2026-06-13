package com.tce.smart.data.api.vo.msg;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 *  App权限详情Vo
 *
 * @author mckaywu
 * @date 2019-06-13 15:06:19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtAppAuthDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -5990298027681118839L;

	/**
	 * 权限ID
	 */
	private Integer id;

	/**
	 * 权限名称
	 */
	private String authName;

	/**
	 * 权限ID
	 */
	private String[] moduleId;

	/**
	 * HR招聘数据权限ID
	 */
	private String[] hrAuthId;

	/**
	 * 是否固定
	 */
	private Boolean isFix;

	/**
	 * 权限描述
	 */
	private String authDesc;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 园区id
	 */
	private Integer parkId;

	/**
	 * 职层id
	 */
	private String[] jcheId;

	private Integer initFlag;
}
