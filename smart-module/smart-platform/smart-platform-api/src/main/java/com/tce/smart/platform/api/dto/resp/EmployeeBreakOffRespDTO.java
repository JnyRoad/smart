package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 调休返回实体类
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
public class EmployeeBreakOffRespDTO implements Serializable {
private static final long serialVersionUID = 1L;


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
