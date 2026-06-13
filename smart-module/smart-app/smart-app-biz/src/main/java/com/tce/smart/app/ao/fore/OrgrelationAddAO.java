package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/19 .
 * @Modified By:
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrgrelationAddAO extends BaseAO{
    private static final long serialVersionUID = 810334330863750158L;
    private List<OrgrelationAO> orgrelation;
    private String applicationId;
}
