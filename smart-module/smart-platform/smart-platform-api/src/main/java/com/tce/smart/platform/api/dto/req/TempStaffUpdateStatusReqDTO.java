package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author fushiping
 * @date 2021/12/21 0021 15:51
 **/
@Data
public class TempStaffUpdateStatusReqDTO extends BaseDTO {

	private List<String> ids;
}
