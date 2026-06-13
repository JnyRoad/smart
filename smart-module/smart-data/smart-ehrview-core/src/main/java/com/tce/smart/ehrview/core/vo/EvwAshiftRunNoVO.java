package com.tce.smart.ehrview.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.Date;


@Data
public class EvwAshiftRunNoVO extends BaseVO {
    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    private String depName;
    /**
     * 公司
     */
    private String compName;
    /**
     * 出行人姓名
     */
    private String pedestrianName;

    /**
     * 出差开始时间
     */
    private Date tripBeginTime;
    /**
     * 出差结束时间
     */
    private Date tripEndTime;
    /**
     * 出差申请时间
     */
    private Date applicationTime;

    /**
     * 流程编号
     */
    private String processNumber;

}
