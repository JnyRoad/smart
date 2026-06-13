package com.tce.smart.ehrview.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class EvwEappraisVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    private String badge;
    private String name;
    private String compId;
    private String compName;
    private String depId;
    private String depName;
    private String jobId;
    private String jobName;
    private String jchenId;
    private String jchenName;
    private String joinDate;
    private String prize;
    private String effectDate;

}
