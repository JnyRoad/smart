package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 请假申请列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchBreakoffApplicationRespDTO implements Serializable {
private static final long serialVersionUID = -5420316448612909761L;

/**
 *
 */
    private Integer recordId;
    /**
   *
   */
    private String staffName;
    /**
     *
     */
    private Integer type;

    private String recordTypeDesc;

    private String restDesc;

    private String restDate;
    /**
     *记录时间
     */
    private Date recordDate;
    private Date workDate;
    private String restCount;
    private String restAbleCount;
    private String processId;

}
