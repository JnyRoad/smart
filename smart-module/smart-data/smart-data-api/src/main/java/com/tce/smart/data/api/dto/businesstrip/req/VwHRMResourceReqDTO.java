package com.tce.smart.data.api.dto.businesstrip.req;

import com.tce.smart.common.core.ao.BaseAO;
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
public class VwHRMResourceReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -3251564314199087461L;

	private Integer id;
	private String workCode;
	private String lastName;
	private String telePhone;
	private String mobile;
	private String email;

}
