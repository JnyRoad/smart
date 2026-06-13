package com.tce.smart.data.api.dto.temporary.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 *
 * @author QIPEI
 *
 */
@Data
public class OcompanyRespDTO extends BaseVO {

	 private static final long serialVersionUID = -5027616915605361578L;

	private Integer CompID;

	private String CompCode;

	private Integer EZID;


}
