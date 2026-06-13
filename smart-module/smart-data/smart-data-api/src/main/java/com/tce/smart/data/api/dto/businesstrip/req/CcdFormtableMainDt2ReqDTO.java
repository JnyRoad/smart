package com.tce.smart.data.api.dto.businesstrip.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;

import java.sql.Date;

/**
 * 出差报告数据
 *
 * @author 梁园
 * @date 2019-06-24
 */
@Data
public class CcdFormtableMainDt2ReqDTO extends BaseAO {
	 private static final long serialVersionUID = -7515000298107096074L;

	    private Integer mainId;

	    private Date tripTime;

	    private String businessTrip;

	    private String workItem;

	    private String expectedEffect;

	    private String confirmDep;

	    private String recommendations;

}
