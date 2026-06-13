package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 补卡查询参数
 *
 * @author 梁圆
 * @date 2019-05-08 18:18:30
 */
@Data
public class SearchPatchReqDTO implements Serializable {
private static final long serialVersionUID = 2463467023495417548L;

    /**
   * 开始时间
   */
    private String patchDate;
    /**
     * 员工号
     */
    private String staffBadge;
}
