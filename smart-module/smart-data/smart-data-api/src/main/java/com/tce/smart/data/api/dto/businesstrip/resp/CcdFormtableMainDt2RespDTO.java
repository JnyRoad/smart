package com.tce.smart.data.api.dto.businesstrip.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.sql.Date;

/**
 * 出差报告数据
 *
 * @author 梁园
 * @date 2019-06-24
 */
@Data
public class CcdFormtableMainDt2RespDTO extends BaseVO {
	 private static final long serialVersionUID = -4113151283385274386L;

	    private Integer mainId;

	    private Date tripTime;

	    private String businessTrip;

	    private String workItem;

	    private String expectedEffect;

	    private String confirmDep;

	    private String recommendations;

}
