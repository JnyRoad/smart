package com.tce.smart.platform.api.dto.req;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 *
 * @author
 * @date 2019-04-13 18:30:08
 */
@Data
public class QuitDorApplyQueryReqDTO implements Serializable {
	private static final long serialVersionUID = -3563221881568564009L;

	private Integer parkId;

	private  Integer status;

	private  Integer isSecurityGuard;

	private String badge;

	private String name;

}
