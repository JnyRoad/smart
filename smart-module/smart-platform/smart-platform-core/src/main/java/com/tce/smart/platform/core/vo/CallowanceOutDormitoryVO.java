package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * 取消补贴时，查询外宿补贴
 * @author QIPEI
 *
 */
@Data
public class CallowanceOutDormitoryVO {


	   /**
     * 补贴开始时间
     */
    private String startTime;

    /**
     * 补贴结束时间
     */
    private String endTime;

    /**
     * 补贴类型
     */
    private String allowanceType;

    /**
     * 补贴金额
     */
    private String amount;


    /**
     * 入职时间
     */
    private String joinDate;

    /**
     * 员工状态
     */
    private String staffStatus;

}
