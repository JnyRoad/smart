package com.tce.smart.app.vo.fore;

import com.tce.smart.platform.api.dto.LeaveHandoverDepJjrDTO;
import lombok.Data;

import java.util.List;

/**
 * 交接项
 * @author Administrator
 *
 */
@Data
public class LeaveItemDataVO {

    private List<LeaveHandoverDepJjrDTO> handover;

	/**
	 * 考勤工时
	 */
	private Integer workDays;

    /**
     * 0：审批中；
	 * 1：通过；
     * 2：拒绝；
	 * 3：交接开始；；
	 * 4：交接完成
     */
    private Integer approveStatus;

}
