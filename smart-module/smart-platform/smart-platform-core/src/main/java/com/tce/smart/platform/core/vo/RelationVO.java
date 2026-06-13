package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/15 .
 * @Modified By:
 */
@Data
public class RelationVO{
    private Integer relationType; //紧急联系人关系
    private String relationTypeDesc; //紧急联系人描述
    private String emergencyName;   //紧急联系人名称
    private String emergencyPhone; //紧急联系人电话

}
