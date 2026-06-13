package com.tce.smart.data.api.dto.ehrview.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * <p>
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class OvwYsjobRespDTO extends BaseVO {

    private static final long serialVersionUID = -8440477002136612992L;

    private String jobid;
    private String jobname;
    private Integer DepID;
    private String depname;
    private Integer JchenID;
    private String JchenName;
    private Integer JXianID;
    private String JxianName;
    private Integer JQunID;
    private String JqunName;
    private Integer JZuID;
    private String JzuName;
    private Integer JZongID;
    private String JzongName;
    private Integer JobType;
    private String jobTypeName;
    private String flCJ;
    private Integer empkind;
    private String empkindName;
    private Integer JCostID;
    private String JcostName;
    private Date timestamp;
    private String ASzstatus;

}
