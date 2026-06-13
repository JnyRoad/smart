package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author
 * @date 2019-04-18 14:32:40
 */
@Data
public class DormitoryQueryNoStaffDTO implements Serializable {
	private static final long serialVersionUID = -7856857977799280259L;

	List<Integer> ids;

}
