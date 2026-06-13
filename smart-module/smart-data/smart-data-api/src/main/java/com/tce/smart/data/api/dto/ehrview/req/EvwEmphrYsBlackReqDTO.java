package com.tce.smart.data.api.dto.ehrview.req;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;


/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class EvwEmphrYsBlackReqDTO extends BaseVO {

    private String badge;
    private String name;
	/**
	 * 身份证号
	 */
	private String cerNo;
}
