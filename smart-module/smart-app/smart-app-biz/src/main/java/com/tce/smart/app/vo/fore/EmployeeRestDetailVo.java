package com.tce.smart.app.vo.fore;

import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调休详情信息VO
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeRestDetailVo extends BaseVO {


    /**
   * 调休日期
   */
    private Date restDate;
    /**
   * 出勤日期
   */
    private Date workDate;
    /**
     * 调休类型
     */
    private String vacateTypeDesc;

    /**
     * 现在要调休天数
     */
    private String restCount;
    /**
     * 可调休天数
     */
    private String restAbleCount;
    /**
     *备注
     */
    private String restDesc;


}
