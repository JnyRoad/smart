package com.tce.smart.data.api.dto.businesstrip.resp;

import com.tce.smart.common.core.ao.BaseAO;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工表
 *
 * @author liangyuan
 * @date 2019-06-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VwHRMResourceRespDTO extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -3484848419398527702L;

	private Integer id;
	private String workCode;
	private String lastName;
	private String telePhone;
	private String mobile;
	private String email;

}
