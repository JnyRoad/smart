package com.tce.smart.data.api.dto.oa.resp;

import lombok.Data;
import java.io.Serializable;

/**
 *
 * @author fushiping
 * @since
 */
@Data
public class WorkflowSelectitemRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	private String ID;
	/**
	 * 区域名
	 */
	private String SELECTNAME;


	private String SELECTVALUE;

}
