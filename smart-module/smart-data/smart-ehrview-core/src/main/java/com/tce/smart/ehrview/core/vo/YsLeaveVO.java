package com.tce.smart.ehrview.core.vo;

import java.util.Date;
import java.util.List;

import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.ehrview.core.entity.EvwEapprais;

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
public class YsLeaveVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    private String badge;

    private String name;

    private Integer compId;

    private Integer depId;

    private String jobId;

    private Integer jchenId;

    private Integer jxianId;

    private Integer leaintent;

    private Date joinDate;

    private String ifPy = CommonConstants.STATUS_NORMAL;

    private String isCgpy = CommonConstants.STATUS_NORMAL;

    private Integer ezId;

    private Integer eId;

    private String yearDay;

    /**
     * 评优记录
     */
    private List<EvwEapprais> evwEapprais;
}
