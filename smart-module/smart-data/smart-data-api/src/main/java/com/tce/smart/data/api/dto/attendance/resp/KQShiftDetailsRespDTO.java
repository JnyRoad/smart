package com.tce.smart.data.api.dto.attendance.resp;

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
public class KQShiftDetailsRespDTO extends BaseVO {

	private String empNo;

	private String empname;


	private String empRunDate;

	private String runNo;

	private String runName;

	private Double runNormalHour;

	private String run1StartTime;

	private String run1EndTime;

	private String run2StartTime;

	private String run2EndTime;

	private String run3StartTime;

	private String run3EndTime;

	private String run4StartTime;

	private String run4EndTime;

	private String run5StartTime;

	private String run5EndTime;

	private String run6StartTime;

	private String run6EndTime;

}
